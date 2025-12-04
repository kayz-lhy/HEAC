package com.kayz.heac.user.dto;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC, staticName = "of")
@NoArgsConstructor(access = AccessLevel.PUBLIC, staticName = "empty")
public class UserLoginVO implements Serializable {
    private String userId;        // 用户真实ID (用于客户端本地缓存，不展示)
    private String token;         // JWT Token (包含身份信息)
    private String account;       // 账号 (掩码处理后)
    private String role;          // 角色
    private Long expireIn;        // Token 过期时间
}