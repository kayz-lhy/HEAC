package com.kayz.heac.user.controller;

import com.kayz.heac.common.dto.UserLoginDTO;
import com.kayz.heac.common.dto.UserLoginVO;
import com.kayz.heac.common.dto.UserRegisterDTO;
import com.kayz.heac.common.entity.HeacResponseEntity;
import com.kayz.heac.common.exception.AuthException;
import com.kayz.heac.user.service.AuthService;
import com.kayz.heac.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserActionController {
    private final UserService userService;
    private final AuthService authService;

    public UserActionController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    // ? is there any of them
    @PostMapping("/register")
    public HeacResponseEntity<String> register(@RequestBody UserRegisterDTO request) {
        try {
            userService.register(request);
            return HeacResponseEntity.success("注册成功");
        } catch (Exception e) {
            return HeacResponseEntity.internalServerError("注册失败", "500");
        }
    }

    /**
     * 用户登录接口
     *
     * @param request 登录请求体
     */
    @PostMapping("/login")
    public HeacResponseEntity<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO, HttpServletRequest request) {
        try {
            UserLoginVO response = authService.login(userLoginDTO, request);
            return HeacResponseEntity.success(response, "登录成功");
        } catch (AuthException e) {
            return HeacResponseEntity.error("500", e.getMessage());
        }
    }

    @PostMapping("/logout")
    public HeacResponseEntity<String> logout(@RequestBody String token) {
        try {
            authService.logout(token);
            return HeacResponseEntity.success("登出成功");
        } catch (AuthException e) {
            return HeacResponseEntity.error("500", e.getMessage());
        }
    }

//    @PostMapping("/updateLastLoginIp")
//    public HeacResponseEntity<String> updateLastLoginIp(@RequestBody String token, @RequestParam("ip") String ip) {
//        userService.updateLastLoginIp(token, ip);
//        return HeacResponseEntity.success("更新成功");
//    }

//    @PostMapping("/updateLastLoginTime")
//    public HeacResponseEntity<String> updateLastLoginTime(@RequestBody String token, @RequestParam("lastLoginTime") LocalDateTime lastLoginTime) {
//        userService.updateLastLoginTime(token, lastLoginTime);
//        return HeacResponseEntity.success("更新成功");
//    }

    @PostMapping("/updateRealNameStatus")
    public HeacResponseEntity<String> updateRealNameStatus(@RequestBody String userId, @RequestParam("realNameStatus") Boolean realNameStatus) {
        try {
            userService.updateRealNameStatus(userId, realNameStatus);
            return HeacResponseEntity.success("更新成功");
        } catch (Exception e) {
            return HeacResponseEntity.error("500", "更新失败:" + e.getMessage());
        }
    }
}
