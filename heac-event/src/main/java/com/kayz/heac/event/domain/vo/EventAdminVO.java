package com.kayz.heac.event.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "事件管理视图")
public class EventAdminVO {
    private String id;
    private String title;
    private String summary;
    private String coverImg;
    private String status; // 枚举转字符串
    private Long heatScore;
    private List<String> tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createdBy;
}
