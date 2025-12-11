package com.kayz.heac.event.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事件发布广播消息 DTO
 * 用于 RocketMQ Topic: event-topic:publish
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MQ消息体-事件发布通知")
public class EventPublishDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "事件ID", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private String eventId;

    @Schema(description = "事件标题 (冗余字段，方便下游展示)", example = "SpaceX 星舰发射成功")
    private String title;

    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    @Schema(description = "初始热度 (可选，默认0)", example = "0")
    private Long initHeatScore;
}
