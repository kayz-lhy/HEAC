package com.kayz.heac.user.service.impl;

import com.kayz.heac.user.entity.User;
import com.kayz.heac.user.service.TokenService;
import com.kayz.heac.user.util.JwtUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.kayz.heac.common.consts.RedisPrefix.TOKEN_CACHE_PREFIX;

@Service
public class TokenServiceImpl implements TokenService {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    public TokenServiceImpl(JwtUtil jwtUtil, RedisTemplate<String, Object> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String createAndCacheToken(User user) {
        // 1. 生成 JWT
        String token = jwtUtil.createToken(user);

        // 2. 存入 Redis (Key: "auth:token:xyz...", Value: userId)
        String redisKey = TOKEN_CACHE_PREFIX + token;
        redisTemplate.opsForValue().set(
                redisKey,
                user.getId(),
                jwtUtil.getAccessTokenExpMinutes(),
                TimeUnit.MINUTES
        );

        return token;
    }

    @Override
    public boolean validateToken(String token) {
        // 双重校验：JWT 签名校验 + Redis 存在性校验 (实现黑名单/踢人下线)
        return jwtUtil.validateToken(token) &&
                redisTemplate.hasKey(TOKEN_CACHE_PREFIX + token);
    }

    @Override
    public void invalidateToken(String token) {
        redisTemplate.delete(TOKEN_CACHE_PREFIX + token);
    }

    @Override
    public String getUserIdFromToken(String token) {
        // 可以直接解密 JWT，或者查 Redis，看你对安全性的要求
        // 这里演示查 Redis，保证只有未过期的 Token 才能换取 ID
        Object userId = redisTemplate.opsForValue().get(TOKEN_CACHE_PREFIX + token);
        return userId != null ? userId.toString() : null;
    }
}