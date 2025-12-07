package com.kayz.heac.user.controller;

import com.kayz.heac.common.context.UserContext;
import com.kayz.heac.common.entity.HeacResponse;
import com.kayz.heac.user.domain.dto.UserLoginDTO;
import com.kayz.heac.user.domain.dto.UserRegisterDTO;
import com.kayz.heac.user.domain.vo.UserLoginVO;
import com.kayz.heac.user.service.AuthService;
import com.kayz.heac.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth") // 1. 统一前缀
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    // 2. 加入 @Valid 开启参数校验
    public HeacResponse<String> register(@RequestBody @Valid UserRegisterDTO request) {
        userService.register(request);
        return HeacResponse.success("注册成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public HeacResponse<UserLoginVO> login(@RequestBody @Valid UserLoginDTO request, HttpServletRequest servletRequest) {
        UserLoginVO vo = authService.login(request, servletRequest);
        return HeacResponse.success(vo);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public HeacResponse<Void> logout() {
        String token = UserContext.getToken();
        authService.logout(token);
        return HeacResponse.success();
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    public HeacResponse<UserLoginVO> refreshToken() {
        String token = UserContext.getToken();
        String userId = UserContext.getUserId();

        UserLoginVO vo = authService.refreshToken(token, userId);
        return HeacResponse.success(vo);
    }
}
