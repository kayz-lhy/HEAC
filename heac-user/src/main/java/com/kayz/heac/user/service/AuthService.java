package com.kayz.heac.user.service;

import com.kayz.heac.common.dto.UserLoginDTO;
import com.kayz.heac.common.dto.UserLoginVO;
import jakarta.servlet.http.HttpServletRequest;


public interface AuthService {
    UserLoginVO login(UserLoginDTO dto, HttpServletRequest request);

    void logout(String token);
}
