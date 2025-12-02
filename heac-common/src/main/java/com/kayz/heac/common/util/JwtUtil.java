package com.kayz.heac.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component("jwtUtil")
public class JwtUtil {

    private final SecretKey key;

    @Getter
    private final long accessTokenExpMinute;

    // 构造器注入配置
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.accessTokenExpirationMinute}") long accessTokenExpMinute) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpMinute = accessTokenExpMinute;
    }

    /**
     * 生成 Token (纯净版)
     * @param userId 用户ID
     * @param claims 其他业务数据 (isAdmin 等)
     */
    public String createToken(String userId, Map<String, Object> claims) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + (long) accessTokenExpMinute * 60 * 1000);

        JwtBuilder builder = Jwts.builder()
                .subject(userId)           // 标准 subject 存 ID
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(key, Jwts.SIG.HS256);

        if (claims != null && !claims.isEmpty()) {
            claims.forEach(builder::claim);
        }

        return builder.compact();
    }

    /**
     * 解析 Claims (只负责解析，不负责检查 Redis)
     * @throws JwtException 解析失败抛出标准异常，由调用方捕获处理
     */
    public Claims getClaimsFromToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 简单校验：仅校验签名和有效期
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("Token 校验失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取过期时间 (用于 Service 层计算 Redis TTL)
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }
}
