package com.kayz.heac.user.interceptor;


import cn.hutool.core.text.CharSequenceUtil;
import com.kayz.heac.common.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@SuppressWarnings("unused")
@Component
@Slf4j
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 网关传过来的 ID
        String userId = request.getHeader("X-User-Id");
        String token = request.getHeader("Authorization");

        log.warn("token = " + token);
        log.warn("id = " + userId);

        if (CharSequenceUtil.isNotBlank(token)) {
            // 放入 ThreadLocal
            token = token.substring(7); // 去掉 Bearer 空格
            UserContext.setToken(token);
        }
        if (CharSequenceUtil.isNotBlank(userId)) {
            // 放入 ThreadLocal
            UserContext.setUserId(userId);
        }
        // 如果为空，说明是白名单接口进来，或者网关漏了（理论不应该），不做处理或抛异常
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear(); // 必须清理
    }
}
