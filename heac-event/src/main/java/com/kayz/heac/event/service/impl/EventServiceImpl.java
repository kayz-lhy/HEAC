package com.kayz.heac.event.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kayz.heac.common.exception.EventException;
import com.kayz.heac.event.cache.EventCacheManager;
import com.kayz.heac.event.dto.EventPublishDTO;
import com.kayz.heac.event.entity.Event;
import com.kayz.heac.event.enums.EventStatus;
import com.kayz.heac.event.mapper.EventMapper;
import com.kayz.heac.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl extends ServiceImpl<EventMapper, Event> implements EventService {

    private final EventCacheManager eventCacheManager;
    private final RocketMQTemplate rocketMQTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createDraft(Event event) {
        // 强制设置初始状态
        event.setStatus(EventStatus.DRAFT);
        event.setHeatScore(0L);
        this.save(event);
        log.info("创建事件草稿: {}", event.getId());
        return event.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishEvent(String id) throws EventException {
        Event event = this.getById(id);
        if (event == null) {
            throw new EventException("事件不存在");
        }

        // 状态流转校验 (可选)
//        if (event.getStatus() != EventStatus.DRAFT && event.getStatus() != EventStatus.WARMUP) {
//            throw new EventStatusException("当前状态不可发布:"+event.getStatus());
//        }

        // 1. 更新 DB
        event.setStatus(EventStatus.PUBLISHED);
        event.setStartTime(LocalDateTime.now());
        this.updateById(event);

        // 2. 清除缓存
        eventCacheManager.invalidate(id);

        // 3. 发送 MQ (异步通知 Opinion 服务准备接客)
        try {
            EventPublishDTO msg = new EventPublishDTO(id, event.getTitle(), LocalDateTime.now());
            rocketMQTemplate.convertAndSend("event-topic:publish", msg);
            log.info("事件发布消息已发送: {}", id);
        } catch (Exception e) {
            log.error("MQ 发送失败，但不回滚事务", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeEvent(String id) {
        // 使用 LambdaUpdate 避免先查后改，性能更好
        boolean success = this.lambdaUpdate().eq(Event::getId, id).set(Event::getStatus, EventStatus.CLOSED).set(Event::getEndTime, LocalDateTime.now()).update();

        if (success) {
            eventCacheManager.invalidate(id);
            log.info("事件已下架: {}", id);
        }
    }

    @Override
    public Event getDetail(String id) {
        // 走多级缓存
        return eventCacheManager.get(id);
    }

    @Override
    public Page<Event> getHotList(int page, int size) {
        // 创建分页对象
        Page<Event> pageParam = new Page<>(page, size);
        // TODO 换成mapper写法以便加缓存
        // 查询条件：状态必须是 PUBLISHED (进行中) 或 ENDED (已结束)
        // 排序：按热度倒序，其次按开始时间倒序
        return this.lambdaQuery().
                in(Event::getStatus, EventStatus.PUBLISHED, EventStatus.ENDED)
                .orderByDesc(Event::getHeatScore)
                .orderByDesc(Event::getStartTime)
                .page(pageParam);

    }
}
