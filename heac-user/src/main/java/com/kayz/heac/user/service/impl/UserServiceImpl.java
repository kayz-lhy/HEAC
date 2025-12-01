package com.kayz.heac.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kayz.heac.common.dto.UserRegisterDTO;
import com.kayz.heac.common.exception.AuthException;
import com.kayz.heac.common.exception.UserActionException;
import com.kayz.heac.user.entity.User;
import com.kayz.heac.user.mapper.UserMapper;
import com.kayz.heac.user.service.UserService;
import com.kayz.heac.user.util.JwtUtil;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * User Service Implementation
 *
 * <p>This service provides comprehensive user management functionality including:
 * <ul>
 *   <li>User registration with account validation and password encryption</li>
 *   <li>User authentication with JWT token generation</li>
 *   <li>Session management using Redis for token storage</li>
 *   <li>User profile updates with distributed transaction support</li>
 * </ul>
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li><b>Security:</b> Passwords are encrypted using PasswordEncoder before storage</li>
 *   <li><b>Distributed Transactions:</b> Uses Seata's @GlobalTransactional for cross-service consistency</li>
 *   <li><b>Token Management:</b> JWT tokens are validated and stored in Redis with expiration</li>
 *   <li><b>Template Pattern:</b> Provides a reusable validateAndUpdateUser method for user updates</li>
 * </ul>
 *
 * <p><b>Dependencies:</b>
 * <ul>
 *   <li>{@link PasswordEncoder} - For secure password hashing and verification</li>
 *   <li>{@link JwtUtil} - For JWT token creation, validation, and claims extraction</li>
 *   <li>{@link UserMapper} - MyBatis-Plus mapper for database operations</li>
 *   <li>{@link RedisTemplate} - For caching tokens and session management</li>
 * </ul>
 *
 * @author kayz
 * @version 1.0
 * @see UserService
 * @see User
 * @since 1.0
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * Password encoder for hashing and verifying user passwords.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * JWT utility for token creation, validation, and claims extraction.
     */
    private final JwtUtil jwtUtil;

    /**
     * MyBatis-Plus mapper for user database operations.
     */
    private final UserMapper userMapper;

    /**
     * Redis template for token caching and session management.
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Constructs a new UserServiceImpl with required dependencies.
     *
     * @param passwordEncoder the password encoder for secure password handling
     * @param jwtUtil         the JWT utility for token operations
     * @param userMapper      the user mapper for database access
     * @param redisTemplate   the Redis template for caching
     */
    public UserServiceImpl(PasswordEncoder passwordEncoder, JwtUtil jwtUtil, UserMapper userMapper, RedisTemplate<String, Object> redisTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Template method for validating token and updating user fields.
     *
     * <p>This method implements the Template Method pattern to provide a reusable
     * workflow for user updates that require token validation. It handles:
     * <ol>
     *   <li>Token validation and user retrieval</li>
     *   <li>Execution of the provided update strategy</li>
     *   <li>Persistence of changes to the database</li>
     * </ol>
     *
     * <p><b>Usage Example:</b>
     * <pre>{@code
     * validateAndUpdateUser(token, user -> user.setLastLoginIp("192.168.1.1"));
     * }</pre>
     *
     * @param token          the JWT token for authentication and user identification
     * @param updateStrategy a consumer function that defines how to update the user object
     * @return an Optional containing success message if update succeeds
     * @throws UserActionException if the user does not exist
     * @throws AuthException       if the token is invalid (thrown by getUserByToken)
     * @see #getUserByToken(String)
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public String validateAndUpdateUser(String token, Consumer<User> updateStrategy) {
        // 1. Validate token and retrieve user
        User user = getUserByToken(token).orElseThrow(() -> new UserActionException("用户不存在"));
        // 2. Execute update strategy
        updateStrategy.accept(user);
        this.updateById(user);

        return "更新成功";
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public User validateUserCredentials(String account, String password) throws AuthException {
        User user = this.lambdaQuery().eq(User::getAccount, account).one();

        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthException("账号或密码错误");
        }

        if (user.getStatus() == User.UserStatus.BANNED) {
            throw new AuthException("账号已被封禁");
        }

        return user;
    }

    /**
     * Registers a new user account.
     *
     * <p>This method performs the complete user registration workflow:
     * <ol>
     *   <li>Validates that the account doesn't already exist</li>
     *   <li>Creates a new User entity with encrypted password and default settings</li>
     *   <li>Persists the user to the database</li>
     *   <li>Returns the generated user ID</li>
     * </ol>
     *
     * <p><b>Default User Settings:</b>
     * <ul>
     *   <li>Verification Status: UNVERIFIED (real-name verification not completed)</li>
     *   <li>User Status: NORMAL (account is active)</li>
     *   <li>Admin: false (regular user, not administrator)</li>
     *   <li>Preferences: Empty JSON object "{}"</li>
     *   <li>Last Login IP: "0.0.0.0" (placeholder, updated on first login)</li>
     * </ul>
     *
     * <p><b>Security:</b> The password is encrypted using {@link PasswordEncoder}
     * before being stored in the database.
     *
     * @param dto the user registration data transfer object containing account and password
     * @return an Optional containing the newly created user's ID
     * @throws UserActionException if the account already exists
     * @see UserRegisterDTO
     * @see User#of(String, String, String, User.VerificationStatus, Timestamp, User.UserStatus, String, String, Timestamp, boolean, Integer, Integer)
     */
    @Transactional(rollbackFor = Exception.class)
    public String register(UserRegisterDTO dto) throws UserActionException {
        // 1. Validate that account doesn't already exist
        boolean exists = this.lambdaQuery().eq(User::getAccount, dto.getAccount()).exists();
        if (exists) {
            throw new UserActionException("账号已存在");
        }

        // 2. Build User object with default settings
        User user = User.builder()
                .account(dto.getAccount())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .status(User.UserStatus.NORMAL)
                .realNameStatus(User.VerificationStatus.UNVERIFIED)
                .createTime(Timestamp.valueOf(LocalDateTime.now()))
                .version(1)
                .build();


        // 3. Persist to database
        try {
            this.save(user);
            return user.getId();
        } catch (DuplicateKeyException e) {
            log.error("注册用户失败, 账号已存在", e);
            throw new UserActionException("账号已存在");
        }
    }


    /**
     * Updates the user's last login IP address.
     *
     * <p>This method uses the template method {@link #validateAndUpdateUser(String, Consumer)}
     * to validate the token and update the user's last login IP address.
     *
     * @param token the JWT token for authentication
     * @param ip    the IP address to set as the last login IP
     * @return an Optional containing success message if update succeeds
     * @throws UserActionException if the user does not exist
     * @throws AuthException       if the token is invalid
     * @see #validateAndUpdateUser(String, Consumer)
     */
//    @GlobalTransactional(rollbackFor = Exception.class)
//    public Optional<String> updateLastLoginIp(String token, String ip) {
//        return validateAndUpdateUser(token, user -> user.setLastLoginIp(ip));
//    }

    /**
     * Retrieves a user by their JWT token.
     *
     * <p>This method:
     * <ol>
     *   <li>Validates the JWT token</li>
     *   <li>Extracts the user_id claim from the token</li>
     *   <li>Queries the database for the user with that ID</li>
     * </ol>
     *
     * <p>This is a core method used by other service methods to authenticate
     * and retrieve user information based on the provided token.
     *
     * @param token the JWT token containing user claims
     * @return an Optional containing the User if found, empty Optional if not found
     * @throws AuthException if the token is invalid or expired
     * @see JwtUtil#validateToken(String)
     * @see JwtUtil#getClaimsFromToken(String)
     */

    public Optional<User> getUserByToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new AuthException("无效的令牌");
        }
        return Optional.ofNullable(this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, jwtUtil.getClaimsFromToken(token).get("user_id", String.class))));
    }


    /**
     * Updates the user's last login timestamp.
     *
     * <p>This method uses the template method {@link #validateAndUpdateUser(String, Consumer)}
     * to validate the token and update the user's last login time.
     *
     * @param token         the JWT token for authentication
     * @param lastLoginTime the timestamp to set as the last login time
     * @return an Optional containing success message if update succeeds
     * @throws UserActionException if the user does not exist
     * @throws AuthException       if the token is invalid
     * @see #validateAndUpdateUser(String, Consumer)
     */
//    @GlobalTransactional(rollbackFor = Exception.class)
//    public Optional<String> updateLastLoginTime(String token, LocalDateTime lastLoginTime) {
//        return validateAndUpdateUser(token, user -> user.setLastLoginTime(Timestamp.valueOf(lastLoginTime)));
//    }

    /**
     * Updates the user's real-name verification status.
     *
     * <p>This method uses the template method {@link #validateAndUpdateUser(String, Consumer)}
     * to validate the token and update the user's real-name verification status.
     *
     * <p>The verification status is set based on the boolean parameter:
     * <ul>
     *   <li>{@code true} → {@link User.VerificationStatus#VERIFIED}</li>
     *   <li>{@code false} or {@code null} → {@link User.VerificationStatus#UNVERIFIED}</li>
     * </ul>
     *
     * @param userId         the JWT token for authentication
     * @param realNameStatus true to mark as verified, false or null to mark as unverified
     * @return an Optional containing success message if update succeeds
     * @throws UserActionException if the user does not exist
     * @throws AuthException       if the token is invalid
     * @see #validateAndUpdateUser(String, Consumer)
     * @see User.VerificationStatus
     */

    public void updateRealNameStatus(String userId, Boolean realNameStatus) {
        this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getRealNameStatus,
                        realNameStatus != null && realNameStatus
                                ? User.VerificationStatus.VERIFIED : User.VerificationStatus.UNVERIFIED)
                .update();
    }
}