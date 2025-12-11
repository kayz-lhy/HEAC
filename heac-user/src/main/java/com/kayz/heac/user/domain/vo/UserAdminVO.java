package com.kayz.heac.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户管理详情视图 (含敏感信息)")
public class UserAdminVO implements Serializable {

    @Schema(description = "用户ID")
    private String id;

    @Schema(description = "账号")
    private String account;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "手机号 (可能脱敏)")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "账号状态")
    private String status;

    @Schema(description = "实名状态")
    private String realNameStatus;

    @Schema(description = "真实姓名 (仅管理员可见)")
    private String realName;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "注册时间")
    private LocalDateTime createTime;
}
