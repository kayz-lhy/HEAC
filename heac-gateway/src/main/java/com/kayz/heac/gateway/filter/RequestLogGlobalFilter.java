package com.kayz.heac.gateway.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.kayz.heac.common.util.IpUtil.getIpAddress;

/**
 * 网关全局请求日志过滤器 (修复版：清洗数据 + 单行JSON)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestLogGlobalFilter implements GlobalFilter, Ordered {

    private static final String START_TIME = "startTime";
    private static final DateTimeFormatter DTM_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        exchange.getAttributes().put(START_TIME, System.currentTimeMillis());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();

            // 使用 LinkedHashMap 保持字段顺序，看着更舒服
            Map<String, Object> logMap = new LinkedHashMap<>();

            // 1. 基础信息
            logMap.put("ts", LocalDateTime.now().format(DTM_FORMAT)); // 缩短字段名为 ts
            logMap.put("id", request.getId());

            // 2. 请求信息
            logMap.put("ip", getIpAddress(request));
            logMap.put("m", request.getMethod().name()); // method -> m
            logMap.put("u", request.getURI().getPath()); // url -> u
            if (request.getURI().getQuery() != null) {
                logMap.put("q", request.getURI().getQuery()); // query -> q
            }

            // 3. 响应与耗时
            Long startTime = exchange.getAttribute(START_TIME);
            long executeTime = (startTime != null) ? (System.currentTimeMillis() - startTime) : 0;
            int statusCode = (response.getStatusCode() != null) ? response.getStatusCode().value() : 500;

            logMap.put("s", statusCode); // status -> s
            logMap.put("t", executeTime); // time -> t (ms)

            // 4. 【关键修复】清洗 User ID 的双引号
            String userId = request.getHeaders().getFirst("X-User-Id");
            if (StrUtil.isNotBlank(userId)) {
                // Redis 序列化可能导致 userId 变成 "uuid"，这里强行去掉引号
                logMap.put("uid", StrUtil.removeAll(userId, '"'));
            }

            // 5. 【关键修复】强制生成单行 JSON
            String jsonLog = JSONUtil.toJsonStr(logMap);

            // 6. 打印日志 (不再显示 null: null 这种前缀，直接打印 JSON)
            if (statusCode >= 400) {
                log.warn(jsonLog);
            } else {
                log.info(jsonLog);
            }
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
