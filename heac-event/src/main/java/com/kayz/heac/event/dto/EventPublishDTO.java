package com.kayz.heac.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事件发布通知消息
 * 用于 RocketMQ: event-topic:publish
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventPublishDTO implements Serializable {

    // 注意：这里要适配 UUID，使用 String 类型
    private String eventId;

    private String title;

    private LocalDateTime publishTime;
}
