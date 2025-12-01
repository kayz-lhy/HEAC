package com.kayz.heac.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kayz.heac.common.dto.UserRegisterDTO;
import com.kayz.heac.common.exception.AuthException;
import com.kayz.heac.common.exception.UserActionException;
import com.kayz.heac.user.entity.User;

public interface UserService extends IService<User> {

    User validateUserCredentials(String account, String passwordHash) throws AuthException;

    String register(UserRegisterDTO dto) throws UserActionException;

    void updateRealNameStatus(String userId, Boolean realNameStatus);
}
