package com.kayz.heac.user.service;

import com.kayz.heac.user.entity.User;

/**
 * Token 管理服务接口
 * 负责 Token 的生命周期管理：生成、存储、验证、销毁
 */
public interface TokenService {
    // 生成并缓存 Token
    String createAndCacheToken(User user);

    // 验证并刷新 Token (续期)
    boolean validateToken(String token);

    // 从 Token 中解析 UserId
    String getUserIdFromToken(String token);

    // 销毁 Token
    void invalidateToken(String token);

    // 获取 Token 的有效期 (分钟)
    Long getAccessTokenExpMinutes();
}