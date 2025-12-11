package com.kayz.heac.event.controller;

import com.kayz.heac.common.entity.HeacResponse;
import com.kayz.heac.common.entity.PageResult;
import com.kayz.heac.event.domain.dto.EventQueryDTO;
import com.kayz.heac.event.domain.vo.EventPortalVO;
import com.kayz.heac.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 前台围观大厅控制器
 * 面向 C 端海量用户，核心接口均经过多级缓存优化
 */
@RestController
@RequestMapping("/event/portal")
@RequiredArgsConstructor
@Validated // 开启参数校验
@Tag(name = "C端-围观大厅", description = "热门事件列表与详情查询")
public class EventPortalController {

    private final EventService eventService;

    @GetMapping("/{id}")
    @Operation(summary = "获取事件详情", description = "高并发接口，走 Caffeine+Redis 多级缓存")
    public HeacResponse<EventPortalVO> getPortalDetail(@PathVariable String id) {
        return HeacResponse.success(eventService.getPortalDetail(id));
    }

    @GetMapping("/hot-list")
    @Operation(summary = "热门围观列表", description = "按热度值倒序，支持标签筛选")
    public HeacResponse<PageResult<EventPortalVO>> getHotList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String tag) {

        // 构造查询对象，复用 Service 层的 query 逻辑
        EventQueryDTO query = new EventQueryDTO();
        query.setPage(page);
        query.setSize(size);
        query.setTag(tag);

        return HeacResponse.success(eventService.getHotList(query));
    }
}
