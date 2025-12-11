package com.kayz.heac.event.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.kayz.heac.event.enums.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "sys_event", autoResultMap = true)
@Schema(description = "热点事件元数据")
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(staticName = "empty")
@Builder
public class Event implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "事件标题")
    @TableField("title")
    @NotBlank(message = "事件标题不能为空") // JSR-303 校验
    private String title;

    @Schema(description = "事件摘要")
    @TableField("summary")
    private String summary;

    @Schema(description = "封面图片URL")
    @TableField("cover_img")
    private String coverImg;

    @Schema(description = "事件状态")
    @TableField("status")
    // 这里的校验逻辑稍微复杂，通常在业务层处理，或者写自定义注解校验枚举
    private EventStatus status;

    @Schema(description = "热度值")
    @TableField("heat_score")
    private Long heatScore;

    @Schema(description = "事件标签列表")
    @TableField(typeHandler = JacksonTypeHandler.class) // 2. 指定类型处理器
    private List<String> tags;

    @Schema(description = "开始围观时间")
    @TableField("start_time")
    private LocalDateTime startTime;

    @Schema(description = "结束围观时间")
    @TableField("end_time")
    private LocalDateTime endTime;

    @Schema(description = "来源类型 (ORIGINAL/WEIBO/...)")
    private String sourceType;

    @Schema(description = "原文链接")
    private String sourceUrl;

    @Schema(description = "原作者 (外部)")
    private String originalAuthor;

    @Schema(description = "发布者昵称 (冗余)")
    private String publisherName;

    @Schema(description = "发布者头像 (冗余)")
    private String publisherAvatar;





    /* ---------------- 审计字段 (自动填充) ---------------- */

    @Schema(description = "创建人ID")
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /* ---------------- 控制字段 (逻辑删除/乐观锁) ---------------- */

    @Schema(description = "乐观锁版本号")
    @Version
    @TableField(value = "version", fill = FieldFill.INSERT)
    private Integer version;

    @Schema(description = "逻辑删除标识")
    @TableLogic(value = "0", delval = "1")
    @TableField(value = "deleted", fill = FieldFill.INSERT)
    private Integer deleted;
}
