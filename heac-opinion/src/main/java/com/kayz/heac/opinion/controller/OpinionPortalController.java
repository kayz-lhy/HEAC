package com.kayz.heac.opinion.controller;

import com.kayz.heac.common.entity.HeacResponse;
import com.kayz.heac.opinion.domain.dto.OpinionPostDTO;
import com.kayz.heac.opinion.domain.vo.OpinionVO;
import com.kayz.heac.opinion.service.OpinionService;
import com.kayz.heac.opinion.util.IpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/opinion/portal")
@RequiredArgsConstructor
@Tag(name = "C端-观点互动")
public class OpinionPortalController {

    private final OpinionService opinionService;

    @PostMapping("/post")
    @Operation(summary = "发表观点", description = "异步接口，立即返回。需携带 Token。")
    public HeacResponse<OpinionVO> post(@RequestBody @Valid OpinionPostDTO dto,
                                        HttpServletRequest request) {

        // 1. 获取审计信息
        String ip = IpUtil.getIpAddress(request); // 需要自己在 Common 实现 getIpAddr
        String ua = request.getHeader("User-Agent");

        // 2. 调用服务
        OpinionVO vo = opinionService.postOpinion(dto, ip, ua);

        return HeacResponse.success(vo);
    }
}
