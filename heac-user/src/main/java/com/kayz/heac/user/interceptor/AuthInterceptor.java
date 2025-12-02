package com.kayz.heac.user.interceptor;

import com.kayz.heac.user.context.UserContext;
import com.kayz.heac.user.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Deprecated(since = "1.0.0")
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;

    public AuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");

        // 简单的 Token 校验
        if (token != null && tokenService.validateToken(token)) {
            String userId = tokenService.getUserIdFromToken(token);
            // ！！！关键一步：将 ID 放入上下文
            UserContext.setUserId(userId);
            return true;
        }

        response.setStatus(401);
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
