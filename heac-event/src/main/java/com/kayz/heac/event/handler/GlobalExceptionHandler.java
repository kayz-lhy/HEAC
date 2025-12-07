package com.kayz.heac.event.handler;

import com.kayz.heac.common.entity.HeacResponse;
import com.kayz.heac.common.exception.EventException;
import com.kayz.heac.common.exception.EventStatusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获参数校验异常 (@NotBlank, @NotNull 等)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public HeacResponse<Void> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String errorMsg = bindingResult.getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", errorMsg);
        return HeacResponse.error(400, errorMsg);
    }

    /**
     * 捕获事件模块状态异常
     */
    @ExceptionHandler(EventStatusException.class)
    public HeacResponse<Void> handleEventStatusException(EventException e) {
        log.error("发生事件状态处理异常", e);
        return HeacResponse.error(1301, e.getMessage());
    }

    /**
     * 捕获事件模块异常
     */
    @ExceptionHandler(EventException.class)
    public HeacResponse<Void> handleEventException(EventException e) {
        log.error("发生事件相关处理异常", e);
        return HeacResponse.error(1301, e.getMessage());
    }

    /**
     * 捕获业务异常 (RuntimeException)
     */
    @ExceptionHandler(RuntimeException.class)
    public HeacResponse<Void> handleRuntimeException(RuntimeException e) {
        log.error("业务异常", e);
        return HeacResponse.error(500, e.getMessage());
    }

    /**
     * 捕获兜底异常
     */
    @ExceptionHandler(Exception.class)
    public HeacResponse<Void> handleException(Exception e) {
        log.error("发生未知系统异常", e);
        return HeacResponse.error(500, "系统出错，请稍后重试");
    }
}
