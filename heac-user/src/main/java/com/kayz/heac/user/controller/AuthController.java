package com.kayz.heac.user.controller;

import com.kayz.heac.common.context.UserContext;
import com.kayz.heac.common.entity.HeacResponse;
import com.kayz.heac.user.domain.dto.UserLoginDTO;
import com.kayz.heac.user.domain.dto.UserRegisterDTO;
import com.kayz.heac.user.domain.vo.UserLoginVO;
import com.kayz.heac.user.service.AuthService;
import com.kayz.heac.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证授权控制器
 * 处理注册、登录、登出、刷新令牌等无需鉴权或仅需基础鉴权的请求
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "C端-认证中心", description = "用户注册、登录、登出管理")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册，账号不可重复")
    public HeacResponse<String> register(@RequestBody @Valid UserRegisterDTO request) {
        userService.register(request);
        return HeacResponse.success("注册成功");
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "账号密码登录，返回 JWT 令牌")
    public HeacResponse<UserLoginVO> login(@RequestBody @Valid UserLoginDTO request,
                                           HttpServletRequest servletRequest) {
        UserLoginVO vo = authService.login(request, servletRequest);
        return HeacResponse.success(vo);
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "使当前 Token 失效 (需携带 Header)")
    public HeacResponse<Void> logout() {
        String token = UserContext.getToken();
        authService.logout(token);
        return HeacResponse.success();
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "在 Token 即将过期时换取新 Token (保活)")
    public HeacResponse<UserLoginVO> refreshToken() {
        String token = UserContext.getToken();
        String userId = UserContext.getUserId();

        UserLoginVO vo = authService.refreshToken(token, userId);
        return HeacResponse.success(vo);
    }
}
