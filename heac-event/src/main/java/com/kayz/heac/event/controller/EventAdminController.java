package com.kayz.heac.event.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kayz.heac.common.entity.HeacResponse;
import com.kayz.heac.common.entity.PageResult;
import com.kayz.heac.event.entity.Event;
import com.kayz.heac.event.service.EventService;
import com.kayz.heac.event.util.PageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/event")
@RequiredArgsConstructor
@Tag(name = "后台-事件管理")
public class EventAdminController {

    private final EventService eventService;

    @PostMapping("/create")
    @Operation(summary = "创建草稿")
    public HeacResponse<String> create(@RequestBody Event event) {
        String id = eventService.createDraft(event);
        return HeacResponse.success(id);
    }

    @PostMapping("/update")
    @Operation(summary = "修改事件")
    public HeacResponse<String> update(@RequestBody Event event) {
        // 只有 updateById 才能触发自动填充 updateTime 和 乐观锁
        boolean success = eventService.updateById(event);
        return success ? HeacResponse.success() : HeacResponse.error(500, "更新失败");
    }

    @PostMapping("/publish/{id}")
    @Operation(summary = "发布上线")
    public HeacResponse<String> publish(@PathVariable String id) {
        eventService.publishEvent(id);
        return HeacResponse.success();
    }

    @PostMapping("/close/{id}")
    @Operation(summary = "强制下架")
    public HeacResponse<String> close(@PathVariable String id) {
        eventService.closeEvent(id);
        return HeacResponse.success();
    }

    @GetMapping("/list")
    @Operation(summary = "后台列表查询(不限状态)")
    public HeacResponse<PageResult<Event>> list(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        // 后台看所有状态
        Page<Event> p = eventService.page(new Page<>(page, size));
        PageResult<Event> res = PageUtils.toPageResult(p);
        return HeacResponse.success(res);
    }
}
