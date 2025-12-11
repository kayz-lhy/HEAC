package com.kayz.heac.event.controller;

import com.kayz.heac.common.entity.HeacResponse;
import com.kayz.heac.common.entity.PageResult;
import com.kayz.heac.event.domain.dto.EventCreateDTO;
import com.kayz.heac.event.domain.dto.EventQueryDTO;
import com.kayz.heac.event.domain.dto.EventUpdateDTO;
import com.kayz.heac.event.domain.vo.EventAdminVO;
import com.kayz.heac.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台事件管理控制器
 * 面向运营人员，提供事件全生命周期管理能力
 */
@RestController
@RequestMapping("/event/admin")
@RequiredArgsConstructor
@Tag(name = "B端-事件管理", description = "事件的增删改查、状态流转与后台列表")
public class EventAdminController {

    private final EventService eventService;

    @PostMapping("/create")
    @Operation(summary = "创建草稿", description = "初始状态为 DRAFT，仅管理员可见")
    public HeacResponse<String> createDraft(@RequestBody @Valid EventCreateDTO dto) {
        return HeacResponse.success(eventService.createDraft(dto));
    }

    @PostMapping("/update")
    @Operation(summary = "更新事件", description = "仅限未结束/未下架的事件，更新后会自动清除缓存")
    public HeacResponse<Void> updateEvent(@RequestBody @Valid EventUpdateDTO dto) {
        eventService.updateEvent(dto);
        return HeacResponse.success();
    }

    @PostMapping("/warmup/{id}")
    @Operation(summary = "开启预热", description = "状态流转：DRAFT -> WARMUP。C端可见并显示倒计时")
    public HeacResponse<Void> warmupEvent(
            @Parameter(description = "事件ID", required = true) @PathVariable String id) {
        eventService.warmupEvent(id);
        return HeacResponse.success();
    }

    @PostMapping("/publish/{id}")
    @Operation(summary = "正式发布", description = "状态流转：WARMUP/DRAFT -> PUBLISHED。触发全站广播")
    public HeacResponse<Void> publishEvent(@PathVariable String id) {
        eventService.publishEvent(id);
        return HeacResponse.success();
    }

    @PostMapping("/close/{id}")
    @Operation(summary = "强制下架", description = "状态流转：ANY -> CLOSED。C端不可见")
    public HeacResponse<Void> closeEvent(@PathVariable String id) {
        eventService.closeEvent(id);
        return HeacResponse.success();
    }

    @GetMapping("/list")
    @Operation(summary = "后台列表查询", description = "支持多维度筛选，默认按创建时间倒序")
    public HeacResponse<PageResult<EventAdminVO>> getAdminList(@Valid EventQueryDTO query) {
        // 利用 Spring MVC 的对象绑定，query 参数会自动从 URL 中解析
        return HeacResponse.success(eventService.getAdminList(query));
    }
}
