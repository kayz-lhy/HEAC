package com.kayz.heac.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kayz.heac.common.exception.AuthException;
import com.kayz.heac.common.exception.UserActionException;
import com.kayz.heac.user.domain.dto.PasswordUpdateDTO;
import com.kayz.heac.user.domain.dto.RealNameDTO;
import com.kayz.heac.user.domain.dto.UserRegisterDTO;
import com.kayz.heac.user.domain.dto.UserUpdateDTO;
import com.kayz.heac.user.domain.vo.UserInfoVO;
import com.kayz.heac.user.entity.User;
import com.kayz.heac.user.mapper.UserMapper;
import com.kayz.heac.user.service.TokenService;
import com.kayz.heac.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 用户核心业务服务实现类
 * <p>
 * 职责边界：
 * 1. 负责用户数据的 CRUD 与状态变更。
 * 2. 负责密码的加密与校验。
 * 3. <b>不负责</b> Token 的解析与生成（这是 AuthService 的职责），但在改密等敏感操作时需协同 TokenService 清理会话。
 *
 * @author kayz
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor // Lombok 注解：自动为 final 字段生成构造器注入，替代 @Autowired
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 密码加密工具 (Spring Security)
     * 用于注册时加密密码，登录/改密时校验密码
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * 数据库操作接口 (MyBatis-Plus)
     */
    private final UserMapper userMapper;

    /**
     * 令牌服务
     * 仅用于在“修改密码”或“封禁”时，强制踢用户下线
     */
    private final TokenService tokenService;

    /**
     * 校验用户凭证（供 AuthService 登录使用）
     *
     * @param account  账号
     * @param password 明文密码
     * @return 校验通过的用户实体
     * @throws AuthException 账号不存在、密码错误或账号被封禁时抛出
     */
    @Override
    public User validateUserCredentials(String account, String password) {
        // 1. 查询用户 (只查一条)
        User user = this.lambdaQuery()
                .eq(User::getAccount, account)
                .one();

        // 2. 校验账号是否存在 & 密码是否匹配 (使用 BCrypt 算法比对)
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            // 为了安全，不告诉前端具体是账号错还是密码错，防止枚举账号
            throw new AuthException("账号或密码错误");
        }

        // 3. 校验账号状态 (风控拦截)
        if (user.getStatus() == User.UserStatus.BANNED) {
            throw new AuthException("账号已被封禁，请联系管理员");
        }

        return user;
    }

    /**
     * 用户注册
     *
     * @param dto 注册信息 DTO
     * @return 新注册的用户 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 开启本地事务
    public String register(UserRegisterDTO dto) {
        // 1. 预检查：逻辑层判断账号是否存在 (为了更友好的报错信息)
        // 虽然数据库有唯一索引，但先查一下能避免数据库抛出底层异常
        boolean exists = this.lambdaQuery()
                .eq(User::getAccount, dto.getAccount())
                .exists();

        if (exists) {
            throw new UserActionException("账号已存在");
        }

        // 2. 构建用户实体 (填充默认值)
        User user = User.builder()
                .account(dto.getAccount())
                // 核心：密码必须加密存储！
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                // 默认状态：正常
                .status(User.UserStatus.NORMAL)
                // 默认实名状态：未认证
                .realNameStatus(User.VerificationStatus.UNVERIFIED)
                .createTime(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        // 3. 写入数据库
        try {
            this.save(user);
            // Mybatis-Plus 会自动将生成的主键回填到 user 对象中
            return user.getId();
        } catch (DuplicateKeyException e) {
            // 4. 并发兜底：如果两个请求同时通过了第一步的检查，数据库唯一索引会拦住第二个
            throw new UserActionException("账号已存在");
        }
    }

    /**
     * 获取用户个人资料
     *
     * @param userId 用户ID (来自 Token 解析结果)
     * @return 脱敏后的用户信息 VO
     */
    @Override
    public UserInfoVO getUserProfile(String userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new UserActionException("用户不存在");
        }

        // 属性拷贝：将 User 实体转为 UserInfoVO (去除密码等敏感字段)
        UserInfoVO vo = UserInfoVO.empty();
        BeanUtils.copyProperties(user, vo);

        // TODO: 如果头像为空，可以在这里设置默认头像 URL

        return vo;
    }

    /**
     * 更新用户基础信息
     * 使用 LambdaUpdateWrapper 实现动态更新，只更新传入的非空字段
     *
     * @param userId 用户ID
     * @param dto    更新信息 DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(String userId, UserUpdateDTO dto) {
        // 链式调用：set(condition, column, value)
        // 只有当 dto.getField() != null 时，才会拼接 SET sql 语句
//        boolean success = this.lambdaUpdate()
//                .eq(User::getId, userId)
//                .set(dto.getNickname() != null, User::getNickname, dto.getNickname())
//                .set(dto.getAvatar() != null, User::getAvatar, dto.getAvatar())
//                .set(dto.getPreferences() != null, User::getPreferencesJson, dto.getPreferences())
//                .update();

        if (true) {
            throw new UserActionException("更新失败，用户可能不存在");
        }

        // TODO: 如果引入了 Redis 缓存用户信息，这里需要删除缓存 (Cache Aside Pattern)
        // redisTemplate.delete("user:profile:" + userId);
    }

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param dto    包含旧密码和新密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String userId, PasswordUpdateDTO dto) {
        // 1. 查用户
        User user = this.getById(userId);
        if (user == null) throw new UserActionException("用户不存在");

        // 2. 校验旧密码是否正确 (这是关键安全步骤)
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw new UserActionException("旧密码不正确");
        }

        // 3. 更新新密码 (加密后存入)
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        this.updateById(user);

        // 4. 安全动作：密码修改后，建议踢掉该用户的所有在线 Session
        // 这样用户必须用新密码重新登录
        // 这里的实现依赖于 TokenService 能否根据 userId 找到 token
        // 如果不能，至少可以记录一个 "lastPasswordChangeTime"，登录时校验
    }

    /**
     * 实名认证
     *
     * @param userId 用户ID
     * @param dto    实名信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyRealName(String userId, RealNameDTO dto) {
        // TODO 1: 调用第三方 API (如阿里云/腾讯云) 验证 姓名+身份证号 是否匹配
        // boolean apiCheckPassed = thirdPartyApi.verify(dto.getRealName(), dto.getIdCard());
        // if (!apiCheckPassed) throw new UserActionException("实名信息不匹配");

        // TODO 2: 检查该身份证号是否已被其他账号占用 (防止多账号实名)

        // 3. 更新数据库状态
        this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getRealNameStatus, User.VerificationStatus.VERIFIED)
                // 生产环境应加密存储身份证号，或仅存储脱敏后的 Hash
                // .set(User::getIdCardHash, encrypt(dto.getIdCard()))
                .update();
    }

    /**
     * 锁定/封禁账号 (风控系统调用)
     *
     * @param account 目标账号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockUserByAccount(String account) {
        this.lambdaUpdate()
                .eq(User::getAccount, account)
                .set(User::getStatus, User.UserStatus.BANNED)
                .update();

        log.warn("账号 {} 因触发风控规则已被系统自动锁定", account);

        // TODO: 强制踢下线 (如果实现了 UserId -> Token 的反查映射)
    }

    /**
     * 更新实名状态 (管理员后台调用)
     *
     * @param userId 用户ID
     * @param realNameStatus true:已认证, false:未认证
     */
    @Override
    public void updateRealNameStatus(String userId, Boolean realNameStatus) {
        User.VerificationStatus status = Boolean.TRUE.equals(realNameStatus)
                ? User.VerificationStatus.VERIFIED
                : User.VerificationStatus.UNVERIFIED;

        this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getRealNameStatus, status)
                .update();
    }
}
