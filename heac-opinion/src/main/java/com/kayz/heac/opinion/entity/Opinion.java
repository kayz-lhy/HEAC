package com.kayz.heac.opinion.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.kayz.heac.opinion.enums.OpinionStandpoint;
import com.kayz.heac.opinion.enums.OpinionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "sys_opinion", autoResultMap = true)
@Schema(description = "观点/评论实体")
public class Opinion implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "观点ID")
    private String id;

    @Schema(description = "关联事件ID")
    private String eventId;

    @Schema(description = "发表用户ID")
    private String userId;

    @Schema(description = "立场 (0:中立, 1:支持, 2:反对)")
    private OpinionStandpoint standpoint;

    @Schema(description = "观点标签列表")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    @Schema(description = "短评内容")
    private String content;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "当前状态")
    private OpinionStatus status;

    @Schema(description = "风控风险分")
    private Integer riskScore;

    @Schema(description = "客户端IP")
    private String clientIp;

    @Schema(description = "客户端UA")
    private String userAgent;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Version
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "乐观锁版本")
    private Integer version;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "逻辑删除")
    private Integer deleted;
}
