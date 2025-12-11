package com.kayz.heac.user.controller.inner;

import com.kayz.heac.common.entity.HeacResponse;
import com.kayz.heac.user.domain.vo.UserInfoVO;
import com.kayz.heac.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inner/user")
@RequiredArgsConstructor
@Tag(name = "内部接口-用户服务", description = "仅供微服务间 Feign 调用，网关层应拦截")
public class UserInnerController {

    private final UserService userService;

    /**
     * 根据ID获取基础信息 (供 Event 服务回显发布者信息)
     */
    @GetMapping("/info/{id}")
    @Operation(summary = "获取用户基础信息", description = "内部调用，不走复杂鉴权")
    public HeacResponse<UserInfoVO> getUserInfo(@PathVariable String id) {
        // 直接调用 Service 的 getUserProfile
        // 注意：这里不需要 UserContext，因为是 Feign 传过来的 ID
        UserInfoVO vo = userService.getUserProfile(id);
        return HeacResponse.success(vo);
    }

    /**
     * 批量获取用户信息 (供 Opinion 服务批量回显评论者)
     * 性能优化关键点
     */
    @PostMapping("/batch-info")
    @Operation(summary = "批量获取用户信息", description = "Map<UserId, InfoVO>")
    public HeacResponse<Map<String, UserInfoVO>> getBatchUserInfo(@RequestBody List<String> userIds) {
        Map<String, UserInfoVO> map = userService.getUserProfileBatch(userIds);
        return HeacResponse.success(map);
    }
}
