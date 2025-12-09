package com.kayz.heac.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kayz.heac.common.entity.HeacResponse;
import com.kayz.heac.user.domain.dto.LockUserDTO;
import com.kayz.heac.user.domain.dto.UserQueryDTO;
import com.kayz.heac.user.domain.vo.UserAdminVO;
import com.kayz.heac.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
@Tag(name = "B端-用户管理", description = "后台运营人员使用的用户管理接口")
public class UserAdminController {

    private final UserService userService;

    /**
     * 1. 用户列表查询 (多维度筛选)
     */
    @GetMapping("/list")
    @Operation(summary = "用户列表查询", description = "支持按账号、昵称、状态、时间范围筛选")
    public HeacResponse<Page<UserAdminVO>> getUserList(@Valid UserQueryDTO query) {
        Page<UserAdminVO> page = userService.queryUserList(query);
        return HeacResponse.success(page);
    }

    /**
     * 2. 获取用户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "用户详情", description = "查看用户完整档案")
    public HeacResponse<UserAdminVO> getUserDetail(@PathVariable String id) {
        UserAdminVO detail = userService.getUserDetailForAdmin(id);
        return HeacResponse.success(detail);
    }

    /**
     * 3. 封禁/锁定用户
     */
    @PostMapping("/lock")
    @Operation(summary = "封禁用户", description = "修改状态为 BANNED 并强制下线")
    public HeacResponse<Void> lockUser(@RequestBody @Valid LockUserDTO dto) {
        // 调用 Service 执行封禁逻辑 (改库 + 删 Token + 记录操作日志)
        userService.lockUser(dto);
        log.warn("管理员封禁了用户: {}, 原因: {}", dto.getUserId(), dto.getReason());
        return HeacResponse.success();
    }

    /**
     * 4. 解封用户
     */
    @PostMapping("/unlock/{id}")
    @Operation(summary = "解封用户", description = "恢复状态为 NORMAL")
    public HeacResponse<Void> unlockUser(@PathVariable String id) {
        userService.unlockUser(id);
        log.info("管理员解封了用户: {}", id);
        return HeacResponse.success();
    }
}
