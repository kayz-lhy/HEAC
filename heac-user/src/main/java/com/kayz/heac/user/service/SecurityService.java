package com.kayz.heac.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kayz.heac.user.domain.dto.LoginLogQueryDTO;
import com.kayz.heac.user.domain.vo.UserLoginLogVO;

public interface SecurityService {
    Page<UserLoginLogVO> queryLoginLogs(LoginLogQueryDTO dto);
}
