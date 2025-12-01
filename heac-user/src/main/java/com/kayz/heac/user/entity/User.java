package com.kayz.heac.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * User 实体（完全适配 PostgreSQL + MyBatis-Plus）
 * 注意点：
 * 1. PostgreSQL 没有 datetime，只能使用 timestamp 类型 —— 对应 Java LocalDateTime。
 * 2. UUID 用 @TableId(type = IdType.ASSIGN_UUID)。
 * 3. 枚举使用字符串存储（@EnumValue）。
 * 4. JSONB 推荐使用 String 存储 + TypeHandler（你后续可加）。
 */
@TableName("sys_user")
@Data
@Accessors(chain = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC, staticName = "of")
@NoArgsConstructor(access = AccessLevel.PUBLIC, staticName = "empty")
@Builder
public class User {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 登录账号（手机号/邮箱）
     */
    @TableField("account")
    private String account;

    /**
     * 密码哈希
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 实名状态（枚举字符串）
     */
    @TableField("real_name_status")
    private VerificationStatus realNameStatus;

    /**
     * 注册时间（PostgreSQL timestamp <-> LocalDateTime）
     */
    @TableField("create_time")
    private Timestamp createTime;

    /**
     * 用户状态
     */
    @TableField("status")
    private UserStatus status;

    /**
     * 偏好设置 JSON（字符串形式，数据库字段为 JSONB）
     */
    @TableField("preferences_json")
    private String preferencesJson;

    /**
     * 最近登录 IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 最近登录时间
     */
    @TableField("last_login_time")
    private Timestamp lastLoginTime;

    /**
     * 是否管理员
     */
    @TableField("is_admin")
    private boolean isAdmin;

    /**
     * 乐观锁
     */
    @TableField("version")
    private Integer version;

    /**
     * 逻辑删除（0/1）
     */
    @TableLogic(value = "deleted", delval = "1")
    private Integer deleted;

    /**
     * 实名校验枚举
     */
    @Getter
    public enum VerificationStatus {
        UNVERIFIED("UNVERIFIED"),
        VERIFIED("VERIFIED");

        @EnumValue
        private final String value;

        VerificationStatus(String value) {
            this.value = value;
        }

    }

    /**
     * 用户状态枚举
     */
    @Getter
    public enum UserStatus {
        NORMAL("NORMAL"),
        FROZEN("FROZEN"),
        BANNED("BANNED");

        @EnumValue
        private final String value;

        UserStatus(String value) {
            this.value = value;
        }

    }
}