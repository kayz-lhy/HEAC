package com.kayz.heac.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kayz.heac.common.context.UserContext;
import com.kayz.heac.common.entity.HeacResponse;
import com.kayz.heac.user.domain.dto.LoginLogQueryDTO;
import com.kayz.heac.user.domain.vo.UserLoginLogVO;
import com.kayz.heac.user.service.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 安全审计控制器
 * 负责查询登录日志、操作日志等安全相关数据
 */
@RestController
@RequestMapping("/user/security")
@RequiredArgsConstructor
@Tag(name = "C端-B端-安全审计", description = "登录日志与安全记录查询")
public class UserSecurityAuditController {

    private final SecurityService securityService;

    /**
     * 【C端】查询我的登录历史
     * 场景：App "我的-设置-登录记录"
     */
    @GetMapping("/logs/login/me")
    @Operation(summary = "查询我的登录历史", description = "仅返回当前用户的 MongoDB 日志")
    public HeacResponse<Page<UserLoginLogVO>> getMyLoginLogs(@Validated LoginLogQueryDTO query) {
        // 1. 强制覆盖 userId，防止查别人
        query.setUserId(UserContext.getUserId());
        // 2. 普通用户只能查自己的账号，account 参数也不应生效
        query.setAccount(null);

        // 调用 Service (Mongo 分页查询)
        return HeacResponse.success(securityService.queryLoginLogs(query));
    }

    /**
     * 【B端】查询全站登录日志 (管理员)
     * 场景：后台管理系统-审计日志
     */
    @GetMapping("/logs/login/admin")
    @Operation(summary = "管理员查询全站日志", description = "支持按账号、IP、时间等多维度筛选")
    public HeacResponse<Page<UserLoginLogVO>> getAdminLoginLogs(@Validated LoginLogQueryDTO query) {
        // 管理员可以传 userId 或 account 进行筛选，不做限制
        return HeacResponse.success(securityService.queryLoginLogs(query));
    }
}
