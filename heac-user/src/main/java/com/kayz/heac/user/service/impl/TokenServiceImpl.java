package com.kayz.heac.user.service.impl;

import com.kayz.heac.common.consts.RedisPrefix;
import com.kayz.heac.common.util.JwtUtil;
import com.kayz.heac.user.entity.User;
import com.kayz.heac.user.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    // 反向索引 Key 前缀: user:token:{userId} -> Set<token>
    private static final String USER_TOKEN_INDEX_PREFIX = "user:token:";

    @Override
    public String createAndCacheToken(User user) {
        // 1. 准备 Payload (Claims)
        Map<String, Object> claims = new HashMap<>();
        claims.put("account", user.getAccount());

        // 适配新 Entity: 判断是否管理员 (简单逻辑：看 tags 是否包含 ADMIN)
        boolean isAdmin = user.getTags() != null && user.getTags().contains("ADMIN");
        claims.put("is_admin", isAdmin);

        // 2. 生成 JWT
        String token = jwtUtil.createToken(user.getId(), claims);

        // 3. 存入 Redis (Token -> UserId)
        String tokenKey = RedisPrefix.TOKEN_CACHE_PREFIX + token;
        long expire = jwtUtil.getAccessTokenExpMinute();

        redisTemplate.opsForValue().set(tokenKey, user.getId(), expire, TimeUnit.MINUTES);

        // 4. [新增] 维护反向索引 (UserId -> Set<Token>)
        // 目的：为了实现 invalidateByUserId (封禁踢人)
        String indexKey = USER_TOKEN_INDEX_PREFIX + user.getId();
        redisTemplate.opsForSet().add(indexKey, token);
        redisTemplate.expire(indexKey, expire, TimeUnit.MINUTES);

        return token;
    }

    @Override
    public boolean validateToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(RedisPrefix.TOKEN_CACHE_PREFIX + token));
    }

    @Override
    public void invalidateToken(String token) {
        String tokenKey = RedisPrefix.TOKEN_CACHE_PREFIX + token;

        // 1. 先查 userId (为了清理反向索引)
        Object userIdObj = redisTemplate.opsForValue().get(tokenKey);

        // 2. 删除主 Key
        log.info("删除TOKEN结果:", redisTemplate.delete(tokenKey));

        // 3. 清理反向索引
        if (userIdObj != null) {
            String indexKey = USER_TOKEN_INDEX_PREFIX + userIdObj.toString();
            redisTemplate.opsForSet().remove(indexKey, token);
        }
    }

    /**
     * 按用户ID踢下线 (封禁时使用)
     */
    @Override
    public void invalidateByUserId(String userId) {
        String indexKey = USER_TOKEN_INDEX_PREFIX + userId;

        // 1. 获取该用户所有活跃的 Token
        Set<Object> tokens = redisTemplate.opsForSet().members(indexKey);

        if (tokens != null && !tokens.isEmpty()) {
            // 2. 遍历删除所有 Token Key
            for (Object tokenObj : tokens) {
                String token = tokenObj.toString();
                redisTemplate.delete(RedisPrefix.TOKEN_CACHE_PREFIX + token);
            }
            // 3. 删除反向索引 Key
            redisTemplate.delete(indexKey);
            log.info("强制踢用户下线: {}, 清理 Token 数: {}", userId, tokens.size());
        }
    }

    @Override
    public Long getExpireSeconds() {
        // 将分钟转为秒返回，方便 VO 展示
        return jwtUtil.getAccessTokenExpMinute() * 60;
    }

    @Override
    public String getUserIdFromToken(String token) {
        Object userId = redisTemplate.opsForValue().get(RedisPrefix.TOKEN_CACHE_PREFIX + token);
        return userId != null ? userId.toString() : null;
    }
}
