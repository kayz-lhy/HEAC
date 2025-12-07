package com.kayz.heac.event.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kayz.heac.event.entity.Event;

public interface EventService extends IService<Event> {

    /**
     * 创建草稿
     *
     * @param event 事件对象
     * @return 事件ID
     */
    String createDraft(Event event);

    /**
     * 发布事件 (上线)
     * 1. 修改状态 -> PUBLISHED
     * 2. 清缓存
     * 3. 发 MQ
     *
     * @param id 事件ID
     */
    void publishEvent(String id);

    /**
     * 下架事件
     *
     * @param id 事件ID
     */
    void closeEvent(String id);

    /**
     * 获取详情 (优先查缓存)
     *
     * @param id 事件ID
     * @return 事件详情
     */
    Event getDetail(String id);

    /**
     * 获取热门事件列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 热门事件分页列表
     */
    Page<Event> getHotList(int page, int size);
}
