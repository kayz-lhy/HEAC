package com.kayz.heac.event.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "事件列表筛选条件")
public class EventQueryDTO implements Serializable {

    @Schema(description = "页码", example = "1", defaultValue = "1")
    private int page = 1;

    @Schema(description = "每页大小", example = "10", defaultValue = "10")
    private int size = 10;

    @Schema(description = "标题关键词 (模糊搜索)", example = "星舰")
    private String title;

    @Schema(description = "标签筛选 (精确匹配)", example = "科技")
    private String tag;

    @Schema(description = "状态筛选 (DRAFT/WARMUP/PUBLISHED/ENDED/CLOSED)", example = "PUBLISHED")
    private String status;
}
