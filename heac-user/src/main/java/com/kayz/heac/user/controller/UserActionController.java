package com.kayz.heac.user.controller;

import com.kayz.heac.common.dto.UserLoginDTO;
import com.kayz.heac.common.dto.UserLoginVO;
import com.kayz.heac.common.dto.UserRegisterDTO;
import com.kayz.heac.common.entity.HeacResponseEntity;
import com.kayz.heac.common.exception.AuthException;
import com.kayz.heac.user.service.AuthService;
import com.kayz.heac.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping
public class UserActionController {
    private final UserService userService;
    private final AuthService authService;

    public UserActionController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * 用户注册接口
     *
     * @param request 注册请求体
     */
    @PostMapping("/register")
    public HeacResponseEntity<String> register(@RequestBody UserRegisterDTO request) {
        try {
            userService.register(request);
            log.info("用户注册成功，账号：{}", request.getAccount());
            return HeacResponseEntity.success("注册成功");
        } catch (AuthException e) {
            log.warn("用户注册失败，账号：{}，原因：{}", request.getAccount(), e.getMessage());
            return HeacResponseEntity.error("400", e.getMessage());
        } catch (Exception e) {
            log.error("用户注册失败，账号：{}，异常：{}", request.getAccount(), e.getMessage());
            return HeacResponseEntity.internalServerError(e.getMessage());
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
            log.info("用户登录成功，账号：{}", userLoginDTO.getAccount());
            return HeacResponseEntity.success(response, "登录成功");
        } catch (AuthException e) {
            log.warn("用户登录失败，账号：{}，原因：{}", userLoginDTO.getAccount(), e.getMessage());
            return HeacResponseEntity.error("400", e.getMessage());
        } catch (Exception e) {
            log.error("用户登录失败，账号：{}，异常：{}", userLoginDTO.getAccount(), e.getMessage());
            return HeacResponseEntity.internalServerError(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public HeacResponseEntity<String> logout(@RequestBody String token) {
        try {
            authService.logout(token);
            log.info("用户登出成功，令牌：{}", token);
            return HeacResponseEntity.success("登出成功");
        } catch (AuthException e) {
            log.warn("用户登出失败，令牌：{}，原因：{}", token, e.getMessage());
            return HeacResponseEntity.error("400", e.getMessage());
        } catch (Exception e) {
            log.error("用户登出失败，令牌：{}，异常：{}", token, e.getMessage());
            return HeacResponseEntity.internalServerError(e.getMessage());
        }
    }

    @PostMapping("/updateRealNameStatus")
    public HeacResponseEntity<String> updateRealNameStatus(@RequestBody String userIdToModify, @RequestParam("realNameStatus") Boolean realNameStatus) {
        try {
            userService.updateRealNameStatus(userIdToModify, realNameStatus);
            log.info("实名认证状态更新成功，用户ID：{}", userIdToModify);
            return HeacResponseEntity.success("更新成功");
        } catch (Exception e) {
            log.error("实名认证状态更新失败，用户ID：{}，异常：{}", userIdToModify, e.getMessage());
            return HeacResponseEntity.internalServerError(e.getMessage());
        }
    }
}
