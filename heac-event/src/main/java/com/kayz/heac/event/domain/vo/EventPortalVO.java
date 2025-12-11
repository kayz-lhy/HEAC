package com.kayz.heac.event.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "C端-事件详情视图")
public class EventPortalVO implements Serializable {

    @Schema(description = "事件ID")
    private String id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "封面图")
    private String coverImg;

    @Schema(description = "详情内容")
    private String content;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "当前状态 (WARMUP/PUBLISHED/ENDED)")
    private String status;

    @Schema(description = "热度值", example = "9999")
    private Long heatScore;

    @Schema(description = "开始/发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "原文链接")
    private String sourceUrl;

    @Schema(description = "原作者")
    private String originalAuthor;

    @Schema(description = "发布者昵称")
    private String publisherName;

    @Schema(description = "发布者头像")
    private String publisherAvatar;

    // 注意：不返回 createdBy, version, deleted 等敏感/无用字段
}
