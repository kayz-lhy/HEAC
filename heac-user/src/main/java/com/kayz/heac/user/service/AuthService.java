package com.kayz.heac.user.service;

import com.kayz.heac.user.domain.dto.UserLoginDTO;
import com.kayz.heac.user.domain.vo.UserLoginVO;
import jakarta.servlet.http.HttpServletRequest;


public interface AuthService {
    UserLoginVO login(UserLoginDTO dto, HttpServletRequest request);

    void logout(String token);

    UserLoginVO refreshToken(String token, String userId);


}

