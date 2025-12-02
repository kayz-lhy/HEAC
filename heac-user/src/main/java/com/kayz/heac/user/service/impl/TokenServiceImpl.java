package com.kayz.heac.user.service.impl;

import com.kayz.heac.common.consts.RedisPrefix;
import com.kayz.heac.common.util.JwtUtil;
import com.kayz.heac.user.entity.User;
import com.kayz.heac.user.service.TokenService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
        // 1. 准备 Payload (Claims)
        // 把 User 对象里的关键信息提取出来，转成 Map
        // 纯净版 JwtUtil 不认识 User 类，只认识 Map
        Map<String, Object> claims = new HashMap<>();
        claims.put("is_admin", user.isAdmin()); // 放入你需要的额外字段
        claims.put("account", user.getAccount());

        // 2. 调用纯净版 JwtUtil 生成 Token
        String token = jwtUtil.createToken(user.getId(), claims);

        // 3. 存入 Redis (白名单/会话模式)
        // Key: "auth:token:xxxx", Value: userId
        // 这一步是 TokenService 的核心职责：管理 Token 的状态
        String redisKey = RedisPrefix.TOKEN_CACHE_PREFIX + token;

        // 注意：这里使用了 jwtUtil.getAccessTokenExpMinutes()，确保 Redis 过期时间与 JWT 一致
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
        // 1. 校验签名和时间 (纯计算，不查库)
        if (!jwtUtil.validateToken(token)) {
            return false;
        }

        // 2. 校验 Redis (状态校验)
        // 如果 Redis 里没有这个 Key，说明用户已登出或被踢下线，即使 JWT 没过期也视为无效
        return Boolean.TRUE.equals(redisTemplate.hasKey(RedisPrefix.TOKEN_CACHE_PREFIX + token));
    }

    @Override
    public void invalidateToken(String token) {
        // 登出逻辑：直接删除 Redis Key
        // 纯净版 JwtUtil 不需要知道怎么注销，这里直接操作 Redis 即可
        redisTemplate.delete(RedisPrefix.TOKEN_CACHE_PREFIX + token);
    }

    @Override
    public String getUserIdFromToken(String token) {
        // 策略：优先查 Redis，确保 Token 是活着的
        Object userId = redisTemplate.opsForValue().get(RedisPrefix.TOKEN_CACHE_PREFIX + token);

        if (userId != null) {
            return userId.toString();
        }

        // 兜底：如果 Redis 查不到 (理论上不应该发生，除非 Redis 挂了或策略改变)，
        // 可以尝试从 JWT 字符串反解析，但这取决于你的安全策略。
        // 这里保持你原本的逻辑：查不到就返回 null
        return null;
    }
}
