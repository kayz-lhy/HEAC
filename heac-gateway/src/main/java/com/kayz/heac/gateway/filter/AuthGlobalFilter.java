package com.kayz.heac.gateway.filter;

import cn.hutool.core.text.CharSequenceUtil;
import com.kayz.heac.common.consts.RedisPrefix;
import com.kayz.heac.common.util.JwtUtil;
import com.kayz.heac.gateway.config.IgnoreUrlsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 全局鉴权过滤器
 */
@Component
@Slf4j
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final List<String> ignoreUrls;

    public AuthGlobalFilter(ReactiveStringRedisTemplate redisTemplate, JwtUtil jwtUtil, IgnoreUrlsConfig ignoreUrlsConfig) {
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;

        this.ignoreUrls = ignoreUrlsConfig.getUrls();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 白名单放行
        if (isIgnoreUrl(path)) {
            return chain.filter(exchange);
        }

        // 2. 获取 Authorization Header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (CharSequenceUtil.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            return buildErrorResponse(exchange, "未登录或非法访问");
        }
        String token = authHeader.substring(7);

        // 3. 校验 JWT 格式和签名
        if (!jwtUtil.validateToken(token)) {
            return buildErrorResponse(exchange, "令牌无效或已过期");
        }


        // 5. 权限校验 (简单的 RBAC)
        if (path.contains("/admin/")) {
            // 从 Redis 获取用户角色 (假设登录时存了 "role": "ADMIN")
            // Redis Key 结构: auth:token:xxxx -> value (可能是 JSON)
            Object isAdmin = jwtUtil.getClaimsFromToken(token).get("is_admin");


            if ("false".equals(isAdmin)) {
                return buildErrorResponse(exchange, "无权访问管理员接口");
            }
        }

        // 4. 校验 Redis (检查是否已注销/踢下线)
        // 注意：这里使用 reactive 的 hasKey，返回的是 Mono<Boolean>
        String redisKey = RedisPrefix.TOKEN_CACHE_PREFIX + token;
        return redisTemplate.hasKey(redisKey)
                .flatMap(hasKey -> {
                    if (Boolean.FALSE.equals(hasKey)) {
                        return buildErrorResponse(exchange, "令牌已失效");
                    }

                    // 5. 从 Redis 或 JWT 获取 UserId (推荐从 Redis 拿，因为我们存的是 value=userId)
                    return redisTemplate.opsForValue().get(redisKey)
                            .flatMap(userId -> {
                                // 6. 【关键】将 UserId 传递给下游微服务
                                // 下游服务不再解析 Token，直接读取 X-User-Id 头
                                ServerHttpRequest newRequest = request.mutate()
                                        .header("X-User-Id", userId)
                                        .build();
                                return chain.filter(exchange.mutate().request(newRequest).build());
                            });
                });


    }

    /**
     * 检查是否是白名单路径
     */
    private boolean isIgnoreUrl(String path) {
        for (String url : ignoreUrls) {
            if (pathMatcher.match(url, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回 JSON 格式的错误信息
     */
    private Mono<Void> buildErrorResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String json = String.format("{\"code\": 401, \"msg\": \"%s\", \"data\": null}", message);
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 优先级，越小越先执行
    }
}
