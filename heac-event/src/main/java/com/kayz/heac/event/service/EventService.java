package com.kayz.heac.event.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kayz.heac.common.entity.PageResult;
import com.kayz.heac.event.domain.dto.EventCreateDTO;
import com.kayz.heac.event.domain.dto.EventQueryDTO;
import com.kayz.heac.event.domain.dto.EventUpdateDTO;
import com.kayz.heac.event.domain.vo.EventAdminVO;
import com.kayz.heac.event.domain.vo.EventPortalVO;
import com.kayz.heac.event.entity.Event;

/**
 * 事件核心服务接口
 */
public interface EventService extends IService<Event> {

    // ==========================================================
    // Command Operations (状态流转与写入)
    // ==========================================================

    /**
     * 创建草稿
     * @param dto 创建参数
     * @return 新生成的事件ID
     */
    String createDraft(EventCreateDTO dto);

    /**
     * 更新事件内容
     * @param dto 更新参数
     */
    void updateEvent(EventUpdateDTO dto);

    /**
     * 开启预热 (DRAFT -> WARMUP)
     *
     * @param id 事件ID
     */
    void warmupEvent(String id);

    /**
     * 正式发布 (DRAFT/WARMUP -> PUBLISHED)
     * 触发全站广播
     * @param id 事件ID
     */
    void publishEvent(String id);

    /**
     * 强制下架 (ANY -> CLOSED)
     * @param id 事件ID
     */
    void closeEvent(String id);

    // ==========================================================
    // Query Operations (查询与展示)
    // ==========================================================

    /**
     * [B端] 后台管理列表
     *
     * @param query 筛选条件
     * @return 管理视图分页
     */
    PageResult<EventAdminVO> getAdminList(EventQueryDTO query);

    /**
     * [C端] 热门围观列表
     *
     * @param query 筛选条件 (支持标签、热度排序)
     * @return 门户视图分页
     */
    PageResult<EventPortalVO> getHotList(EventQueryDTO query);

    /**
     * [C端] 获取事件详情
     * 走多级缓存，仅返回公开状态的事件
     * @param id 事件ID
     * @return 门户视图
     */
    EventPortalVO getPortalDetail(String id);
}
