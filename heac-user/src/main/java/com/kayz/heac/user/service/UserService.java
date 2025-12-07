package com.kayz.heac.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kayz.heac.user.domain.dto.PasswordUpdateDTO;
import com.kayz.heac.user.domain.dto.RealNameDTO;
import com.kayz.heac.user.domain.dto.UserRegisterDTO;
import com.kayz.heac.user.domain.dto.UserUpdateDTO;
import com.kayz.heac.user.domain.vo.UserInfoVO;
import com.kayz.heac.user.entity.User;

public interface UserService extends IService<User> {
    User validateUserCredentials(String account, String password);

    String register(UserRegisterDTO dto);

    UserInfoVO getUserProfile(String userId);

    void updateProfile(String userId, UserUpdateDTO dto);

    void changePassword(String userId, PasswordUpdateDTO dto);

    void verifyRealName(String userId, RealNameDTO dto);

    void lockUserByAccount(String account);

    void updateRealNameStatus(String userId, Boolean status);
}
