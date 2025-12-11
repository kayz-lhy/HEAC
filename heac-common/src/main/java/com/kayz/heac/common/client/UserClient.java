package com.kayz.heac.common.client;

import com.kayz.heac.common.entity.HeacResponse;
import com.kayz.heac.common.vo.UserInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "heac-user", path = "/inner/user")
public interface UserClient {

    @GetMapping("/info/{id}")
    HeacResponse<UserInfoVO> getUserInfo(@PathVariable("id") String id);

    @PostMapping("/batch-info")
    HeacResponse<Map<String, UserInfoVO>> getBatchUserInfo(@RequestBody List<String> userIds);
}
