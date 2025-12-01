package com.kayz.heac.user.util;

import com.kayz.heac.common.exception.AuthException;
import com.kayz.heac.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    private final SecretKey key;
    @Getter
    private final int accessTokenExpMinutes;
    private final String redisBlacklistPrefix;
    @Getter
    private final String anonymousIdKey;
    private final RedisTemplate<String, Object> redisTemplate;

    // 通过构造器注入配置
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-minutes}") int accessTokenExpMinutes,
            @Value("${jwt.redis-blacklist-prefix}") String redisBlacklistPrefix,
            @Value("${jwt.anonymous-id-key}") String anonymousIdKey,
            @Autowired RedisTemplate<String, Object> redisTemplate) {
        // 使用 HS256 算法生成安全密钥
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpMinutes = accessTokenExpMinutes;
        this.redisBlacklistPrefix = redisBlacklistPrefix;
        this.anonymousIdKey = anonymousIdKey;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 根据用户数据生成 JWT Token
     *
     * @param user 用户实体（需包含 ID, isAdmin 状态）
     * @return 生成的 JWT 字符串
     */
    public String createToken(User user) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + (long) accessTokenExpMinutes * 60 * 1000);

        // JWT Payload 声明
        return Jwts.builder()
                .subject(user.getId())                                   // Sub (主题): 用户真实 ID (UUID)
                .claim("user_id", user.getId())                          // 自定义用户ID
                .claim("is_admin", user.isAdmin())                       // 权限
                .issuedAt(now)                                           // Iat (签发时间)
                .expiration(expirationDate)                              // Exp (过期时间)
                .signWith(key, Jwts.SIG.HS256)                           // 使用密钥和 HMAC_SHA256 签名
                .compact();
    }

    /**
     * 解析 JWT Token 并返回 Claims
     * 1. 验证签名和有效期
     * 2. 检查 Redis 黑名单
     *
     * @param token JWT 字符串
     * @return Claims 对象
     */
    public Claims getClaimsFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(key.getEncoded()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 检查 Redis 黑名单
            if (isTokenBlacklisted(token)) {
                throw new AuthException("Token已被注销或封禁");
            }
            return claims;

        } catch (ExpiredJwtException e) {
            throw new AuthException("Token已过期", e);
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
            throw new AuthException("无效的Token格式", e);
        } catch (SecurityException e) {
            throw new AuthException("Token签名无效", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims.getExpiration().before(new Date())) {
                throw new AuthException("Token已过期");
            }
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException("无效的Token", e);
        }
        return true;
    }

    /**
     * 检查 Token 是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        // 使用 Token 本身作为 Redis Key，或者使用 JTI（推荐JTI，如果生成时包含）
        String redisKey = redisBlacklistPrefix + token;
        return redisTemplate.hasKey(redisKey);
    }

    /**
     * 注销 Token：将其放入 Redis 黑名单
     *
     * @param token 需要注销的 Token
     */
    public void invalidateToken(String token) {
        try {
            // 解析出剩余有效期
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

            Date expiration = claims.getExpiration();
            Date now = new Date();

            // 计算剩余有效期 (毫秒)
            long remainingMillis = expiration.getTime() - now.getTime();

            if (remainingMillis > 0) {
                String redisKey = redisBlacklistPrefix + token;
                // 将 Token 存入 Redis，TTL 设置为剩余有效期
                redisTemplate.opsForValue().set(redisKey, "invalidated", remainingMillis, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            // 如果解析失败，可能是无效或已过期的 Token，忽略或记录日志
            System.err.println("无法解析 Token 进行注销: " + e.getMessage());
        }
    }

    public <T> Optional<T> validateAndDo(String token, java.util.function.Function<Claims, T> handler) {
        return validateToken(token) ? Optional.ofNullable(handler.apply(getClaimsFromToken(token))) : Optional.empty();
    }

}