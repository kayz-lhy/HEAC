package com.kayz.heac.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录响应视图")
public class UserLoginVO implements Serializable {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "登录账号")
    private String account;

    @Schema(description = "JWT 访问令牌")
    private String token;

    @Schema(description = "令牌有效期 (秒)")
    private Long expireIn;

    @Schema(description = "用户基础信息 (头像/昵称等)")
    private UserInfoVO userInfo;
}
