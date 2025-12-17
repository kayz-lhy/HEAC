package com.kayz.heac.opinion.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IpUtil {

    /**
     * 获取客户端真实 IP 地址
     * 注意：只依赖 X-Forwarded-For 在某些反向代理配置下容易被伪造
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = null;

        // 常见的几种代理头顺序检查
        String[] headerNames = {
                "X-Forwarded-For", // 多级代理链
                "X-Real-IP",      // Nginx default
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headerNames) {
            String value = request.getHeader(header);
            if (value != null && !value.isEmpty() && !"unknown".equalsIgnoreCase(value)) {
                // X-Forwarded-For 可能返回多个 IP，取链路第一个
                if (value.contains("")) {
                    value = value.split(",")[0].trim();
                }
                ip = value;
                break;
            }
        }

        // headers 均无效时获取远程地址
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip != null ? ip : "unknown";
    }
}