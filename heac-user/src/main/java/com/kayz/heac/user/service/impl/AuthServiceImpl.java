package com.kayz.heac.user.service.impl;

import com.kayz.heac.common.consts.RedisPrefix;
import com.kayz.heac.common.dto.UserLoginDTO;
import com.kayz.heac.common.dto.UserLoginVO;
import com.kayz.heac.common.exception.AuthException;
import com.kayz.heac.common.exception.UserActionException;
import com.kayz.heac.common.util.JwtUtil;
import com.kayz.heac.user.entity.User;
import com.kayz.heac.user.service.AuthService;
import com.kayz.heac.user.service.TokenService;
import com.kayz.heac.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {
    private final TokenService tokenService;
    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;

    public AuthServiceImpl(TokenService tokenService, UserService userService, RedisTemplate<String, Object> redisTemplate) {
        this.tokenService = tokenService;
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }

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
     * @see JwtUtil#createToken(User)
     */

    @Override
    public UserLoginVO login(UserLoginDTO dto, HttpServletRequest request) throws AuthException {
        // 1. Retrieve user by account
        User user = userService.validateUserCredentials(dto.getAccount(), dto.getPassword());

        // 3. Check account status
        if (user.getStatus() == User.UserStatus.BANNED) {
            throw new AuthException("账号已被封禁");
        }

        // 4. Update login information (timestamp) - synchronous for simplicity
        Timestamp lastLoginTime = Timestamp.valueOf(LocalDateTime.now());
        redisTemplate.opsForHash().put(RedisPrefix.LOGIN_TIME_CACHE_KEY, user.getId(), lastLoginTime);
        redisTemplate.opsForHash().put(RedisPrefix.LOGIN_IP_CACHE_KEY, user.getId(), request.getRemoteAddr());

        // 5. Generate JWT Token with user claims (userId, role, realNameStatus)
        String token = tokenService.createAndCacheToken(user);

        // 6. Build and return login response
        return UserLoginVO.builder()
                .userId(user.getId())
                .account(user.getAccount())
                .token(token)
                .role(user.isAdmin() ? "ADMIN" : "USER")
                .build();
    }

    @Override
    public void logout(String token) {
        tokenService.invalidateToken(token);
    }
}
