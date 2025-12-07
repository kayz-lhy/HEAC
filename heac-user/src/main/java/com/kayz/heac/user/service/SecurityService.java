package com.kayz.heac.user.service;

import com.kayz.heac.user.domain.vo.UserLoginLogVO;

import java.util.List;

public interface SecurityService {
    List<UserLoginLogVO> getLoginLogs(int pageNum, int pageSize, String userId, String account, String ip, Integer status);
}
