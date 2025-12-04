package com.kayz.heac.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kayz.heac.common.exception.AuthException;
import com.kayz.heac.common.exception.UserActionException;
import com.kayz.heac.common.util.JwtUtil;
import com.kayz.heac.user.dto.UserRegisterDTO;
import com.kayz.heac.user.entity.User;
import com.kayz.heac.user.mapper.UserMapper;
import com.kayz.heac.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
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
 * 用户服务实现类
 * 处理用户注册、身份认证以及个人信息管理等核心逻辑。
 *
 * @author kayz
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RocketMQTemplate rocketMQTemplate;
    /**
     * 安全验证并更新用户信息的模板方法。
     * 集成了 Seata 以支持分布式事务回滚。
     *
     * @param token          JWT 身份令牌
     * @param updateStrategy 包含具体 User 实体修改逻辑的 Consumer 函数
     * @return 操作成功提示信息
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public String validateAndUpdateUser(String token, Consumer<User> updateStrategy) {
        // 1. 验证 Token 并获取用户
        User user = getUserByToken(token)
                .orElseThrow(() -> new UserActionException("用户不存在"));

        // 2. 执行更新策略
        updateStrategy.accept(user);
        this.updateById(user);

        return "更新成功";
    }

    @Override
    public User validateUserCredentials(String account, String password) throws AuthException {
        // 校验账号是否存在
        User user = this.lambdaQuery()
                .eq(User::getAccount, account)
                .one();

        // 校验密码
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthException("账号或密码错误");
        }

        // 校验用户状态
        if (user.getStatus() == User.UserStatus.BANNED) {
            throw new AuthException("账号已被封禁");
        }

        return user;
    }

    /**
     * 注册新用户。
     * 包含重复账号检查和密码加密逻辑。
     *
     * @param dto 注册信息传输对象
     * @return 新生成的用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public String register(UserRegisterDTO dto) throws UserActionException {
        // 快速失败：检查账号是否已存在
        if (exists(Wrappers.<User>lambdaQuery().eq(User::getAccount, dto.getAccount()))) {
            throw new UserActionException("账号已存在");
        }

        // 构建用户对象，设置默认值
        User user = User.builder()
                .account(dto.getAccount())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .status(User.UserStatus.NORMAL)
                .realNameStatus(User.VerificationStatus.UNVERIFIED)
                .createTime(Timestamp.valueOf(LocalDateTime.now()))
                .version(1)
                .build();

        try {
            this.save(user);
            return user.getId();
        } catch (DuplicateKeyException e) {
            // 并发安全兜底：捕获数据库唯一索引冲突
            log.error("注册失败：账号 {} 已存在。", dto.getAccount());
            throw new UserActionException("账号已存在");
        }
    }

    /**
     * 根据 JWT Token 获取用户详情。
     *
     * @param token JWT 令牌
     * @return 包含用户的 Optional 对象
     */
    public Optional<User> getUserByToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new AuthException("无效的令牌");
        }

        String userId = jwtUtil.getClaimsFromToken(token).get("user_id", String.class);
        // 使用 MyBatis-Plus 原生方法查询 ID，效率更高
        return Optional.ofNullable(this.getById(userId));
    }

    /**
     * 更新实名认证状态。
     *
     * @param userId         目标用户 ID
     * @param realNameStatus true 为已认证，false 或 null 为未认证
     */
    @Override
    public void updateRealNameStatus(String userId, Boolean realNameStatus) {
        User.VerificationStatus status = (realNameStatus != null && realNameStatus)
                ? User.VerificationStatus.VERIFIED
                : User.VerificationStatus.UNVERIFIED;

        this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getRealNameStatus, status)
                .update();
    }

    @Override
    public void lockUserByAccount(String account) {
        // 1. 查询用户
        User user = userMapper.findUserByAccount(account);
        if (user != null && user.getStatus() == User.UserStatus.NORMAL) {
            // 2. 更新状态
            user.setStatus(User.UserStatus.BANNED);
            this.updateById(user);
        }
        // TODO 其他审查逻辑
        log.info("账号 {} 已被锁定", account);
    }


}
