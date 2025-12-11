package com.kayz.heac.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kayz.heac.user.domain.dto.*;
import com.kayz.heac.user.domain.vo.UserAdminVO;
import com.kayz.heac.user.domain.vo.UserInfoVO;
import com.kayz.heac.user.entity.User;

import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    // ================= C 端接口 (用户前台) =================

    /**
     * 校验凭证 (登录用)
     */
    User validateUserCredentials(String account, String password);

    /**
     * 用户注册
     */
    String register(UserRegisterDTO dto);

    /**
     * 获取个人资料 (脱敏)
     */
    UserInfoVO getUserProfile(String userId);

    Map<String, UserInfoVO> getUserProfileBatch(List<String> userIds);

    /**
     * 更新基础资料
     */
    void updateProfile(String userId, UserUpdateDTO dto);

    /**
     * 修改密码
     */
    void changePassword(String userId, PasswordUpdateDTO dto);

    /**
     * 实名认证提交
     */
    void verifyRealName(String userId, RealNameDTO dto);


    // ================= B 端接口 (管理后台) =================

    /**
     * 后台用户列表查询 (支持分页、筛选)
     */
    Page<UserAdminVO> queryUserList(UserQueryDTO query);

    /**
     * 后台获取用户详情 (包含敏感风控信息)
     */
    UserAdminVO getUserDetailForAdmin(String userId);

    /**
     * 封禁/锁定用户
     */
    void lockUser(LockUserDTO dto);

    /**
     * 解封用户
     */
    void unlockUser(String userId);
}
