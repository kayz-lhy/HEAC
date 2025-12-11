package com.kayz.heac.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kayz.heac.common.exception.AuthException;
import com.kayz.heac.common.exception.UserActionException;
import com.kayz.heac.user.domain.dto.*;
import com.kayz.heac.user.domain.vo.UserAdminVO;
import com.kayz.heac.user.domain.vo.UserInfoVO;
import com.kayz.heac.user.entity.User;
import com.kayz.heac.user.enums.RealNameStatus;
import com.kayz.heac.user.enums.UserStatus;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户核心服务实现
 * 集成了 C端（注册/个人中心）与 B端（后台管理）逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final TokenService tokenService;

    // =================================================================================
    // C 端核心业务 (注册、登录校验、个人中心)
    // =================================================================================

    @Override
    public User validateUserCredentials(String account, String password) {
        User user = this.lambdaQuery()
                .select(User::getId, User::getAccount, User::getPasswordHash, User::getStatus, User::getLockTime) // 指定查询字段提高性能
                .eq(User::getAccount, account)
                .one();

        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthException("账号或密码错误");
        }
        if (user.getStatus() == UserStatus.BANNED) {
            throw new AuthException("账号已被封禁");
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            if (user.getLockTime() != null && user.getLockTime().isAfter(LocalDateTime.now())) {
                throw new AuthException("账号已被临时锁定，请稍后再试");
            } else {
                // 自动解锁逻辑 (可选)
                // this.unlockUser(user.getId());
            }
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String register(UserRegisterDTO dto) {
        boolean exists = this.lambdaQuery().eq(User::getAccount, dto.getAccount()).exists();
        if (exists) throw new UserActionException("账号已存在");

        User user = User.empty();
        user.setAccount(dto.getAccount());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        // 设置默认值 (枚举)
        user.setStatus(UserStatus.NORMAL);
        user.setRealNameStatus(RealNameStatus.UNVERIFIED);
        // createTime 等审计字段由 MyMetaObjectHandler 自动填充，无需手动 set

        try {
            this.save(user);
            return user.getId();
        } catch (DuplicateKeyException e) {
            throw new UserActionException("账号已存在");
        }
    }

    @Override
    public UserInfoVO getUserProfile(String userId) {
        User user = this.getById(userId);
        if (user == null) throw new UserActionException("用户不存在");

        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);

        // 枚举转 String (供前端展示)
        if (user.getRealNameStatus() != null) {
            vo.setRealNameStatus(user.getRealNameStatus().getCode());
        }
        return vo;
    }

    @Override
    public Map<String, UserInfoVO> getUserProfileBatch(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 1. MyBatis Plus 批量查询
        List<User> users = this.listByIds(userIds);

        // 2. 转换 VO 并转为 Map
        return users.stream().map(user -> {
            UserInfoVO vo = new UserInfoVO();
            BeanUtils.copyProperties(user, vo);
            if (user.getRealNameStatus() != null) {
                vo.setRealNameStatus(user.getRealNameStatus().getCode());
            }
            return vo;
        }).collect(Collectors.toMap(UserInfoVO::getId, vo -> vo));
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(String userId, UserUpdateDTO dto) {
        this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(dto.getNickname() != null, User::getNickname, dto.getNickname())
                .set(dto.getAvatar() != null, User::getAvatar, dto.getAvatar())
                .set(dto.getBio() != null, User::getBio, dto.getBio())
                // .set(dto.getPreferences() != null, User::getPreferences, ...) // JSONB 处理需转换
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String userId, PasswordUpdateDTO dto) {
        User user = this.getById(userId);
        if (user == null) throw new UserActionException("用户不存在");

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw new UserActionException("旧密码不正确");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        this.updateById(user);

        // 修改密码后无需手动踢下线，Token 验证逻辑若依赖 DB 密码哈希变更则自动失效
        // 或者调用 tokenService.invalidateByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyRealName(String userId, RealNameDTO dto) {
        // 模拟调用三方API验证...
        this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getRealNameStatus, RealNameStatus.VERIFIED)
                .set(User::getRealName, dto.getRealName()) // 建议加密存储
                .update();
    }

    // =================================================================================
    // B 端管理业务 (后台列表、封禁、详情)
    // =================================================================================

    @Override
    public Page<UserAdminVO> queryUserList(UserQueryDTO query) {
        Page<User> pageParam = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .like(StrUtil.isNotBlank(query.getAccount()), User::getAccount, query.getAccount())
                .like(StrUtil.isNotBlank(query.getNickname()), User::getNickname, query.getNickname())
                // 假设 query.getStatus() 是 String, 这里可以不转枚举直接查，取决于 DB 和 Entity 映射
                // 如果 Entity 用了 @EnumValue，这里建议先把 String 转 Enum
                // .eq(query.getStatus() != null, User::getStatus, UserStatus.valueOf(query.getStatus()))
                .orderByDesc(User::getCreateTime);

        Page<User> result = this.page(pageParam, wrapper);

        // 转换 VO
        return (Page<UserAdminVO>) result.convert(user -> {
            UserAdminVO vo = new UserAdminVO();
            BeanUtils.copyProperties(user, vo);
            if (user.getStatus() != null) vo.setStatus(user.getStatus().getCode());
            if (user.getRealNameStatus() != null) vo.setRealNameStatus(user.getRealNameStatus().getCode());
            return vo;
        });
    }

    @Override
    public UserAdminVO getUserDetailForAdmin(String userId) {
        User user = this.getById(userId);
        if (user == null) throw new UserActionException("用户不存在");

        UserAdminVO vo = new UserAdminVO();
        BeanUtils.copyProperties(user, vo);
        if (user.getStatus() != null) vo.setStatus(user.getStatus().getCode());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockUser(LockUserDTO dto) {
        this.lambdaUpdate()
                .eq(User::getId, dto.getUserId())
                .set(User::getStatus, UserStatus.BANNED)
                .set(dto.getDurationMinutes() != null, User::getLockTime, LocalDateTime.now().plusMinutes(dto.getDurationMinutes()))
                .update();

        log.warn("用户 {} 被管理员封禁，原因: {}", dto.getUserId(), dto.getReason());
        // TODO: 踢下线逻辑
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockUser(String userId) {
        boolean success = this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getStatus, UserStatus.NORMAL)
                .set(User::getLockTime, null)
                .update();

        if (!success) throw new UserActionException("解封失败");
    }
}
