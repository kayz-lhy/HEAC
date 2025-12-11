package com.kayz.heac.user.config;

import com.kayz.heac.common.entity.HeacResponse;
import com.kayz.heac.common.exception.AuthException;
import com.kayz.heac.common.exception.UserActionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(UserActionException.class)
    public HeacResponse<Void> handleUserActionException(UserActionException e) {
        // 业务异常通常是预期的，打印 WARN 即可，不打堆栈
        log.warn("用户操作异常: {}", e.getMessage());

        return HeacResponse.error(400, e.getMessage());
    }

    // 捕获业务异常 (如：密码错误)
    @ExceptionHandler(AuthException.class)
    public HeacResponse<Void> handleAuthException(AuthException e) {
        // 业务异常通常是预期的，打印 WARN 即可，不打堆栈
        log.warn("认证失败: {}", e.getMessage());
        e.printStackTrace();
        return HeacResponse.error(401, e.getMessage());
    }

    // 捕获参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public HeacResponse<Void> handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        return HeacResponse.error(400, msg);
    }

    // 捕获所有未处理异常 (兜底)
    @ExceptionHandler(Exception.class)
    public HeacResponse<Void> handleException(Exception e) {
        // 系统异常必须打印 ERROR 和 堆栈，方便排查
        log.error("系统未知异常", e);
        return HeacResponse.error(500, "系统繁忙，请稍后重试");
    }
}
