package com.kayz.heac.event.status;

import com.kayz.heac.event.entity.Event;
import com.kayz.heac.event.enums.EventStatus;
import org.springframework.stereotype.Component;

/**
 * 事件状态机管理器
 * 定义合法的状态流转路径，防止非法操作
 */
@Component
public class EventStatusManager {

    /**
     * 校验是否允许修改内容
     * 规则：只要没下架(CLOSED) 且 没结束(ENDED)，都可以改
     * (哪怕是 PUBLISHED 也可以修错别字，但不能大改)
     */
    public void ensureCanUpdate(Event event) {
        if (event.getStatus() == EventStatus.CLOSED || event.getStatus() == EventStatus.ENDED) {
            throw new RuntimeException("事件已结束或下架，禁止修改内容");
        }
    }

    /**
     * 校验是否允许预热
     * 路径：DRAFT -> WARMUP
     */
    public void ensureCanWarmup(Event event) {
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new RuntimeException("当前状态[" + event.getStatus().getDesc() + "]不可开启预热，仅草稿状态支持");
        }
    }

    /**
     * 校验是否允许发布
     * 路径：DRAFT -> PUBLISHED
     * 路径：WARMUP -> PUBLISHED
     */
    public void ensureCanPublish(Event event) {
        if (event.getStatus() == EventStatus.PUBLISHED) {
            // 幂等处理：已经是发布状态，直接返回，不报错
            return;
        }
        if (event.getStatus() != EventStatus.DRAFT && event.getStatus() != EventStatus.WARMUP) {
            throw new RuntimeException("当前状态[" + event.getStatus().getDesc() + "]不可发布，请检查是否已下架");
        }
    }

    /**
     * 校验是否允许下架
     * 路径：ANY -> CLOSED (管理员强制操作，拥有最高优先级)
     */
    public void ensureCanClose(Event event) {
        if (event.getStatus() == EventStatus.CLOSED) {
            // 幂等
            return;
        }
    }
}
