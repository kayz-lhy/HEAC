package com.kayz.heac.user.service.impl;

import com.kayz.heac.common.consts.RedisPrefix;
import com.kayz.heac.common.dto.UserLoginLogDTO;
import com.kayz.heac.common.exception.AuthException;
import com.kayz.heac.common.exception.UserActionException;
import com.kayz.heac.user.dto.UserLoginDTO;
import com.kayz.heac.user.dto.UserLoginVO;
import com.kayz.heac.user.entity.User;
import com.kayz.heac.user.service.AuthService;
import com.kayz.heac.user.service.TokenService;
import com.kayz.heac.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final TokenService tokenService;
    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RocketMQTemplate rocketMQTemplate;

    /**
     * Authenticates a user and creates a new session.
     *
     * <p>This method performs the complete user login workflow:
     * <ol>
     *   <li>Retrieves the user by account name</li>
     *   <li>Validates the provided password against the stored hash</li>
     *   <li>Checks if the account is banned</li>
     *   <li>Updates the last login timestamp</li>
     *   <li>Generates a JWT token with user claims</li>
     *   <li>Caches the token in Redis with expiration</li>
     *   <li>Returns login information including token and role</li>
     * </ol>
     *
     * <p><b>Security Features:</b>
     * <ul>
     *   <li>Password verification using secure hashing (PasswordEncoder)</li>
     *   <li>Account status validation (prevents banned users from logging in)</li>
     *   <li>JWT token generation with embedded user claims (userId, role, realNameStatus)</li>
     *   <li>Token caching in Redis with automatic expiration</li>
     * </ul>
     *
     * <p><b>Note:</b> The login timestamp update is performed synchronously.
     * For high-traffic applications, consider using asynchronous processing.
     *
     * @param dto the login credentials containing account and password
     * @return an Optional containing UserLoginVO with userId, account, token, and role
     * @throws UserActionException if account doesn't exist, password is incorrect, or account is banned
     * @see UserLoginDTO
     * @see UserLoginVO
     */

    @Override
    public UserLoginVO login(UserLoginDTO dto, HttpServletRequest request) throws AuthException {
        // 1. Retrieve user by account
        User user = null;
        try {
            user = userService.validateUserCredentials(dto.getAccount(), dto.getPassword());
        } catch (AuthException e) {
            sendLoginLogMessage(buildLoginLog(user, dto.getAccount(), request));
            throw new AuthException("用户密码校验失败");
        }

        // 3. Check account status
        if (user.getStatus() == User.UserStatus.BANNED) {
            throw new AuthException("账号已被封禁, 请与管理员联系");
        }

        // 4. Update login information (timestamp) - synchronous for simplicity
        Timestamp lastLoginTime = Timestamp.valueOf(LocalDateTime.now());
        redisTemplate.opsForHash().put(RedisPrefix.LOGIN_TIME_CACHE_KEY, user.getId(), lastLoginTime);
        redisTemplate.opsForHash().put(RedisPrefix.LOGIN_IP_CACHE_KEY, user.getId(), request.getRemoteAddr());

        // 5. Generate JWT Token with user claims (userId, role, realNameStatus)
        String token = tokenService.createAndCacheToken(user);

        sendLoginLogMessage(buildLoginLog(user, dto.getAccount(), request));
        // 6. Build and return login response
        return UserLoginVO.builder()
                .userId(user.getId())
                .account(user.getAccount())
                .token(token)
                .role(user.isAdmin() ? "ADMIN" : "USER")
                .expireIn(tokenService.getAccessTokenExpMinutes())
                .build();
    }

    @Override
    public void logout(String token) {
        tokenService.invalidateToken(token);
    }


    private void sendLoginLogMessage(UserLoginLogDTO user) {
        try {
            log.info("准备发送登录日志消息: userId={}, account={}, ip={}", user.getUserId(), user.getAccount(), user.getIp());
            // 发送消息
            // 参数1: topic:tag (主题:标签)
            // 参数2: 消息体 (会自动序列化为 JSON)
            rocketMQTemplate.convertAndSend("user-topic:login", user);
            log.info("登录日志消息已发送: {}", user);

        } catch (Exception e) {
            // ！！！关键点：日志发送失败不应该影响用户登录成功
            // 只需要记录错误日志，后续排查即可
            log.error("发送登录日志消息失败", e);
        }
    }

    private UserLoginLogDTO buildLoginLog(User user, String account, HttpServletRequest request) {
        if (user == null) {
            return UserLoginLogDTO.builder()
                    .userId(null)
                    .account(account)
                    .ip(request.getRemoteAddr())
                    .loginTime(LocalDateTime.now())
                    .build();
        }
        return UserLoginLogDTO.builder()
                .userId(user.getId())
                .account(account)
                .ip(request.getRemoteAddr())
                .loginTime(LocalDateTime.now())
                .build();
    }
}
