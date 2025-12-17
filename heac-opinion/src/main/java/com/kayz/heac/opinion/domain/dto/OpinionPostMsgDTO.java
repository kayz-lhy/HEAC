package com.kayz.heac.opinion.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MQ消息-观点发表")
public class OpinionPostMsgDTO implements Serializable {

    @Schema(description = "观点ID (预生成)")
    @NotBlank
    private String id;

    @Schema(description = "事件ID")
    @NotBlank
    private String eventId;

    @Schema(description = "用户ID")
    @NotBlank
    private String userId;

    @Schema(description = "立场")
    @NotNull
    private Integer standpoint;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "IP")
    private String clientIp;

    @Schema(description = "Agent")
    private String userAgent;

    @Schema(description = "创建时间")
    @NotNull
    private LocalDateTime createTime;
}
