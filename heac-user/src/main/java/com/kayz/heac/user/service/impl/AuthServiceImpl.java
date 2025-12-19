package com.kayz.heac.user.service.impl;

import com.kayz.heac.common.dto.UserLoginLogDTO;
import com.kayz.heac.common.exception.AuthException;
import com.kayz.heac.user.domain.dto.UserLoginDTO;
import com.kayz.heac.user.domain.vo.UserInfoVO;
import com.kayz.heac.user.domain.vo.UserLoginVO;
import com.kayz.heac.user.entity.User;
import com.kayz.heac.user.mq.LoginLogProducer;
import com.kayz.heac.user.service.AuthService;
import com.kayz.heac.user.service.TokenService;
import com.kayz.heac.user.service.UserService;
import com.kayz.heac.user.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务实现
 * 负责：登录流程编排、令牌管理、登录日志发送
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final TokenService tokenService;
    private final LoginLogProducer loginLogProducer;

    @Override
    public UserLoginVO login(UserLoginDTO loginDto, HttpServletRequest request) {
        // 1. 核心凭证校验 (委托给 UserService)
        User user = userService.validateUserCredentials(loginDto.getAccount(), loginDto.getPassword());

        // 2. 颁发令牌 (生成 JWT 并存入 Redis)
        String token = tokenService.createAndCacheToken(user);
        // 3. 发送异步登录日志 (RocketMQ)
        // 不阻塞主线程，极大提升登录接口响应速度
        sendLoginLogAsync(user, request);

        // 4. 组装返回视图
        // 包含 Token 和 基础用户信息，方便前端直接展示
        UserInfoVO userInfo = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfo);

        return UserLoginVO.builder()
                .userId(user.getId())
                .account(user.getAccount())
                .token(token)
                // 假设 TokenService 提供了获取过期时间的方法，或者硬编码配置值
                .expireIn(tokenService.getExpireSeconds())
                .userInfo(userInfo)
                .build();
    }

    @Override
    public void logout(String token) {
        // 登出只需销毁 Token，无需查库
        if (token != null) {
            tokenService.invalidateToken(token);
        }
    }

    @Override
    public UserLoginVO refreshToken(String oldToken, String userId) {
        // 1. 校验用户有效性
        User user = userService.getById(userId);
        if (user == null) {
            throw new AuthException("用户不存在");
        }

        // 2. 销毁旧令牌 (防止双重活跃)
        tokenService.invalidateToken(oldToken);

        // 3. 颁发新令牌
        String newToken = tokenService.createAndCacheToken(user);

        // 4. 返回新信息
        UserInfoVO userInfo = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfo);

        return UserLoginVO.builder()
                .userId(user.getId())
                .token(newToken)
                .expireIn(tokenService.getExpireSeconds())
                .userInfo(userInfo)
                .build();
    }

    /**
     * 异步发送登录日志消息到 MQ
     */
    private void sendLoginLogAsync(User user, HttpServletRequest request) {
        try {
            String ip = IpUtil.getIpAddress(request); // 使用工具类获取真实 IP

            UserLoginLogDTO logDTO = UserLoginLogDTO.builder()
                    .userId(user.getId())
                    .account(user.getAccount())
                    .ip(ip)
                    .loginTime(LocalDateTime.now())
                    .build();
            loginLogProducer.sendLoginLogMessage(logDTO);

        } catch (Exception e) {
            // 日志发送失败不应阻断登录流程，仅记录错误供运维排查
            log.error("发送登录日志MQ失败: userId={}", user.getId(), e);
        }
    }
}
