package com.kayz.heac.user.controller;

import com.kayz.heac.common.context.UserContext;
import com.kayz.heac.common.entity.HeacResponse;
import com.kayz.heac.user.domain.dto.PasswordUpdateDTO;
import com.kayz.heac.user.domain.dto.RealNameDTO;
import com.kayz.heac.user.domain.dto.UserUpdateDTO;
import com.kayz.heac.user.domain.vo.UserInfoVO;
import com.kayz.heac.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;

    /**
     * 获取我的个人信息
     */
    @GetMapping("/me")
    public HeacResponse<UserInfoVO> getMyProfile() {
        // 1. 直接从上下文获取 ID
        String userId = UserContext.getUserId();

        // 2. 读操作通常不需要打印 INFO 日志，否则日志量太大
        UserInfoVO profile = userService.getUserProfile(userId);
        return HeacResponse.success(profile);
    }

    /**
     * 更新基础信息 (昵称、头像等)
     */
    @PutMapping("/update")
    public HeacResponse<Void> updateMyProfile(@RequestBody @Valid UserUpdateDTO dto) {
        String userId = UserContext.getUserId();

        userService.updateProfile(userId, dto);
        return HeacResponse.success();
    }

    /**
     * 实名认证
     */
    @PostMapping("/realname")
    public HeacResponse<Void> verifyRealName(@RequestBody @Valid RealNameDTO dto) {
        String userId = UserContext.getUserId();

        // 实名认证属于用户属性变更，建议放在 UserService 而不是 AuthService
        // 且不需要传 Token，Service 只需校验身份证逻辑和修改 DB
        userService.verifyRealName(userId, dto);
        return HeacResponse.success();
    }

    /**
     * 修改密码
     */
    @PostMapping("/password")
    public HeacResponse<Void> changePassword(@RequestBody @Valid PasswordUpdateDTO dto) {
        String userId = UserContext.getUserId();

        // 传入旧密码和新密码的 DTO
        userService.changePassword(userId, dto);
        return HeacResponse.success();
    }
}
