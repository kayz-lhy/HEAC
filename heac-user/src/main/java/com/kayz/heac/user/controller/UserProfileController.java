package com.kayz.heac.user.controller;

import com.kayz.heac.common.context.UserContext;
import com.kayz.heac.common.entity.HeacResponse;
import com.kayz.heac.user.domain.dto.PasswordUpdateDTO;
import com.kayz.heac.user.domain.dto.RealNameDTO;
import com.kayz.heac.user.domain.dto.UserUpdateDTO;
import com.kayz.heac.user.domain.vo.UserInfoVO;
import com.kayz.heac.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/profile")
@RequiredArgsConstructor
@Tag(name = "C端-个人中心", description = "用户个人资料查询与修改")
public class UserProfileController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "返回头像、昵称、实名状态等脱敏信息")
    public HeacResponse<UserInfoVO> getMyProfile() {
        String userId = UserContext.getUserId();
        // 这一步如果抛出异常(如用户被删)，全局异常处理器会捕获
        UserInfoVO profile = userService.getUserProfile(userId);
        return HeacResponse.success(profile);
    }

    @PutMapping("/update")
    @Operation(summary = "更新基础资料", description = "修改昵称、头像、简介、偏好设置")
    public HeacResponse<Void> updateMyProfile(@RequestBody @Valid UserUpdateDTO dto) {
        String userId = UserContext.getUserId();
        userService.updateProfile(userId, dto);
        return HeacResponse.success();
    }

    @PostMapping("/realname")
    @Operation(summary = "实名认证", description = "提交姓名和身份证号进行认证")
    public HeacResponse<Void> verifyRealName(@RequestBody @Valid RealNameDTO dto) {
        String userId = UserContext.getUserId();
        userService.verifyRealName(userId, dto);
        return HeacResponse.success();
    }

    @PostMapping("/password")
    @Operation(summary = "修改密码", description = "修改成功后强制下线")
    public HeacResponse<Void> changePassword(@RequestBody @Valid PasswordUpdateDTO dto) {
        String userId = UserContext.getUserId();
        userService.changePassword(userId, dto);
        return HeacResponse.success();
    }
}
