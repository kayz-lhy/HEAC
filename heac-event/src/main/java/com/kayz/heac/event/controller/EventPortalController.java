package com.kayz.heac.event.controller;

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
@RequestMapping("/portal/event")
@RequiredArgsConstructor
@Tag(name = "前台-围观大厅")
public class EventPortalController {

    private final EventService eventService;

    @GetMapping("/{id}")
    @Operation(summary = "获取事件详情(走多级缓存)")
    public HeacResponse<Event> getDetail(@PathVariable String id) {
        // 这个方法内部走了 Caffeine -> Redis -> DB
        Event event = eventService.getDetail(id);
        return HeacResponse.success(event);
    }

    @GetMapping("/hot-list")
    @Operation(summary = "热门围观列表")
    public HeacResponse<PageResult<Event>> getHotList(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        // TODO 这里的查询未来可以用 Redis ZSet 优化，目前先走 DB 分页
        PageResult<Event> res = PageUtils.toPageResult(eventService.getHotList(page, size));
        return HeacResponse.success(res);
    }

    @GetMapping("/test")
    public HeacResponse<String> test() {
        return HeacResponse.success("Event Portal Service is running!");
    }
}
