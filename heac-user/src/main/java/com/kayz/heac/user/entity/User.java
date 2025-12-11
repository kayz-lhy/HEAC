package com.kayz.heac.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.kayz.heac.user.enums.RealNameStatus;
import com.kayz.heac.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(staticName = "empty")
@TableName(value = "sys_user", autoResultMap = true) // 开启 ResultMap 以支持 JSONB
@Schema(description = "系统用户核心实体")
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /* ------------------- 核心身份 ------------------- */
    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "用户ID (UUID)")
    private String id;

    @Schema(description = "登录账号 (唯一)")
    private String account;

    @Schema(description = "密码凭证 (BCrypt加密)")
    @TableField(select = false) // 默认查询不带出密码，提升安全性
    private String passwordHash;

    @Schema(description = "手机号 (唯一/加密存储)")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    /* ------------------- 公开画像 ------------------- */
    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "头像URL")
    @TableField(fill = FieldFill.INSERT)
    private String avatar;

    @Schema(description = "个人简介")
    private String bio;

    /* ------------------- 实名与合规 ------------------- */
    @Schema(description = "实名认证状态")
    private RealNameStatus realNameStatus;

    @Schema(description = "真实姓名 (脱敏/加密)")
    private String realName;

    /* ------------------- 状态与风控 ------------------- */
    @Schema(description = "账号状态 (NORMAL/LOCKED/BANNED)")
    private UserStatus status;

    @Schema(description = "锁定截止时间 (风控临时封禁使用)")
    private LocalDateTime lockTime;

    @Schema(description = "注册IP")
    private String registerIp;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    /* ------------------- 扩展属性 (PG JSONB) ------------------- */
    @Schema(description = "用户偏好设置 (JSONB)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> preferences;

    @Schema(description = "用户标签 (JSONB, 如: VIP, 媒体, 风险用户)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /* ------------------- 审计字段 ------------------- */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
