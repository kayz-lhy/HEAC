package com.kayz.heac.user.controller;

import com.kayz.heac.common.context.UserContext;
import com.kayz.heac.common.entity.HeacResponse;
import com.kayz.heac.user.domain.vo.UserLoginLogVO;
import com.kayz.heac.user.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user/security")
@RequiredArgsConstructor
public class UserSecurityAuditController {

    private final SecurityService securityService;

    @GetMapping("/logs/login")
    public HeacResponse<List<UserLoginLogVO>> getLoginLogs(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String account,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) Integer status) {
        return HeacResponse.
                success(securityService.getLoginLogs(pageNum, pageSize, userId, account, ip, status));
    }

    @GetMapping("/logs/login/user")
    public HeacResponse<List<UserLoginLogVO>> getUserLoginLogs(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) Integer status) {
        String userId = UserContext.getUserId();
        return HeacResponse.
                success(securityService.getLoginLogs(pageNum, pageSize, userId, null, ip, status));
    }

}
