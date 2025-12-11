package com.kayz.heac.event.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kayz.heac.common.client.UserClient;
import com.kayz.heac.common.context.UserContext;
import com.kayz.heac.common.entity.HeacResponse;
import com.kayz.heac.common.entity.PageResult;
import com.kayz.heac.common.vo.UserInfoVO;
import com.kayz.heac.event.cache.EventCacheManager;
import com.kayz.heac.event.domain.dto.EventCreateDTO;
import com.kayz.heac.event.domain.dto.EventPublishDTO;
import com.kayz.heac.event.domain.dto.EventQueryDTO;
import com.kayz.heac.event.domain.dto.EventUpdateDTO;
import com.kayz.heac.event.domain.vo.EventAdminVO;
import com.kayz.heac.event.domain.vo.EventPortalVO;
import com.kayz.heac.event.entity.Event;
import com.kayz.heac.event.enums.EventStatus;
import com.kayz.heac.event.mapper.EventMapper;
import com.kayz.heac.event.service.EventService;
import com.kayz.heac.event.status.EventStatusManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl extends ServiceImpl<EventMapper, Event> implements EventService {

    private final EventCacheManager cacheManager;
    private final EventStatusManager statusManager;
    private final RocketMQTemplate rocketMQTemplate;
    private final UserClient userClient;

    // =================================================================================================
    // Region: 状态流转与写入 (State Transition & Write)
    // =================================================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createDraft(EventCreateDTO dto) {
        Event event = Event.empty();
        BeanUtils.copyProperties(dto, event);

        // --- 核心逻辑增强 Start ---

        // A. 处理来源默认值
        if (StrUtil.isBlank(event.getSourceType())) {
            event.setSourceType("ORIGINAL"); // 默认为原创
            event.setOriginalAuthor("HEAC官方");
        }

        // B. 填充发布者信息 (调用 User 服务)
        fillPublisherInfo(event);

        // --- 核心逻辑增强 End ---

        event.setStatus(EventStatus.DRAFT);
        event.setHeatScore(0L);

        this.save(event);
        return event.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEvent(EventUpdateDTO dto) {
        getEventOrThrow(dto.getId(), event -> {
            // 1. 状态前置校验
            statusManager.ensureCanUpdate(event);

            // 2. 更新逻辑
            BeanUtils.copyProperties(dto, event);
            this.updateById(event);

            // 3. 缓存一致性
            cacheManager.invalidate(event.getId());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void warmupEvent(String id) {
        processStateChange(id, statusManager::ensureCanWarmup, event -> {
            event.setStatus(EventStatus.WARMUP);
            // 预热不更新 StartTime，那是正式发布的时间
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishEvent(String id) {
        processStateChange(id, statusManager::ensureCanPublish, event -> {
            event.setStatus(EventStatus.PUBLISHED);
            event.setStartTime(LocalDateTime.now());

            // 发布副作用: 发送 MQ
            sendPublishMessage(event);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeEvent(String id) {
        processStateChange(id, statusManager::ensureCanClose, event -> {
            event.setStatus(EventStatus.CLOSED);
            event.setEndTime(LocalDateTime.now());
        });
    }

    /**
     * 通用状态变更模板方法 (核心抽象)
     *
     * @param id        事件ID
     * @param validator 状态校验逻辑
     * @param mutation  状态变更逻辑 (修改 Entity 字段)
     */
    private void processStateChange(String id, Consumer<Event> validator, Consumer<Event> mutation) {
        getEventOrThrow(id, event -> {
            // 1. 校验
            validator.accept(event);

            // 2. 变更
            mutation.accept(event);
            this.updateById(event);

            // 3. 清缓存
            cacheManager.invalidate(id);
            log.info("Event state changed: id={}, status={}", id, event.getStatus());
        });
    }

    // =================================================================================================
    // Region: 查询与展示 (Query & VO)
    // =================================================================================================

    @Override
    public PageResult<EventAdminVO> getAdminList(EventQueryDTO query) {
        return executeQuery(query, wrapper -> {
            // 管理员看所有数据
            if (StrUtil.isNotBlank(query.getTitle())) {
                wrapper.like(Event::getTitle, query.getTitle());
            }
            if (StrUtil.isNotBlank(query.getStatus())) {
                wrapper.eq(Event::getStatus, EventStatus.valueOf(query.getStatus()));
            }
            wrapper.orderByDesc(Event::getCreateTime);
        }, this::convertToAdminVO);
    }

    @Override
    public PageResult<EventPortalVO> getHotList(EventQueryDTO query) {
        return executeQuery(query, wrapper -> {
            // C端只看公开数据
            wrapper.in(Event::getStatus, EventStatus.WARMUP, EventStatus.PUBLISHED, EventStatus.ENDED);

            // 标签筛选 (JSONB)
            if (StrUtil.isNotBlank(query.getTag())) {
                wrapper.apply("tags @> {0}::jsonb", "[\"" + query.getTag() + "\"]");
            }

            // 排序: 热度 > 时间
            wrapper.orderByDesc(Event::getHeatScore).orderByDesc(Event::getStartTime);
        }, this::convertToPortalVO);
    }

    // todo 分开 admin 和 portal
    @Override
    public EventPortalVO getPortalDetail(String id) {
        return Optional.ofNullable(cacheManager.get(id))
                // 状态过滤: 仅允许公开状态
                .filter(e -> Arrays.asList(EventStatus.WARMUP, EventStatus.PUBLISHED, EventStatus.ENDED).contains(e.getStatus()))
                .map(this::convertToPortalVO)
                .orElseThrow(() -> new RuntimeException("内容不存在或未公开"));
    }

    // =================================================================================================
    // Region: Helper Methods
    // =================================================================================================

    /**
     * 统一查询执行器
     */
    private <R> PageResult<R> executeQuery(EventQueryDTO query, Consumer<LambdaQueryWrapper<Event>> criteriaBuilder, Function<Event, R> mapper) {
        Page<Event> pageParam = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Event> wrapper = new LambdaQueryWrapper<>();

        criteriaBuilder.accept(wrapper);

        Page<Event> result = this.page(pageParam, wrapper);

        List<R> list = result.getRecords().stream().map(mapper).collect(Collectors.toList());
        return PageResult.of(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    private void getEventOrThrow(String id, Consumer<Event> action) {
        Event event = Optional.ofNullable(this.getById(id))
                .orElseThrow(() -> new RuntimeException("事件不存在"));
        action.accept(event);
    }

    private void sendPublishMessage(Event event) {
        try {
            EventPublishDTO msg = EventPublishDTO.builder()
                    .eventId(event.getId())
                    .title(event.getTitle())
                    .publishTime(LocalDateTime.now())
                    .build();
            rocketMQTemplate.convertAndSend("event-topic:publish", msg);
        } catch (Exception e) {
            log.error("MQ广播失败: {}", event.getId(), e);
        }
    }

    // VO 转换逻辑
    private EventAdminVO convertToAdminVO(Event e) {
        EventAdminVO vo = new EventAdminVO();
        BeanUtils.copyProperties(e, vo);
        if (e.getStatus() != null) vo.setStatus(e.getStatus().getCode());
        return vo;
    }

    private EventPortalVO convertToPortalVO(Event e) {
        EventPortalVO vo = new EventPortalVO();
        BeanUtils.copyProperties(e, vo);
        if (e.getStatus() != null) vo.setStatus(e.getStatus().getCode());
        return vo;
    }

    /**
     * 提取私有方法：填充发布者信息
     */
    private void fillPublisherInfo(Event event) {
        String currentUserId = UserContext.getUserId();
        if (StrUtil.isBlank(currentUserId)) {
            // 可能是定时任务或系统自动创建，给个默认值
            event.setPublisherName("System");
            return;
        }

        try {
            // Feign 远程调用 (注意处理异常，防止 User 服务挂了导致 Event 没法创建)
            HeacResponse<UserInfoVO> response = userClient.getUserInfo(currentUserId);

            if (response != null && response.getData() != null) {
                UserInfoVO user = response.getData();
                event.setPublisherName(user.getNickname());
                event.setPublisherAvatar(user.getAvatar());
            } else {
                event.setPublisherName("Unknown Admin");
            }
        } catch (Exception e) {
            log.warn("填充发布者信息失败，降级处理: userId={}, error={}", currentUserId, e.getMessage());
            event.setPublisherName("Admin-" + currentUserId.substring(0, 4));
        }
    }
}
