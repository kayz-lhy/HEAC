package com.kayz.heac.event;

import com.kayz.heac.common.context.UserContext;
import com.kayz.heac.event.entity.Event;
import com.kayz.heac.event.enums.EventStatus;
import com.kayz.heac.event.service.EventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EventServiceTest {
    private static String eventId;
    @Resource
    private EventService eventService;

    @BeforeAll
    static void setup() {
        UserContext.setUserId("SERVICE_TEST_USER");
    }

    @Test
    @Order(1)
    @DisplayName("1. 业务流程：创建草稿 -> 发布 -> 缓存读取")
    void testLifecycle() {
        // 1. 创建草稿
        Event draft = Event.empty();
        draft.setTitle("Service Layer Test");
        draft.setSummary("Testing full lifecycle");

        eventId = eventService.createDraft(draft);
        assertNotNull(eventId);

        // 验证状态
        Event dbEvent = eventService.getById(eventId);
        assertEquals(EventStatus.DRAFT, dbEvent.getStatus());

        // 2. 发布事件
        // 注意：这里会发 MQ，如果你本地没起 RocketMQ 可能会报错或卡顿
        // 建议在 application-test.yml 里把 RocketMQ 关掉或者 mock 掉
        try {
            eventService.publishEvent(eventId);
        } catch (Exception e) {
            log.warn("MQ 发送可能失败了，但这不影响业务逻辑测试: {}", e.getMessage());
        }

        Event publishedEvent = eventService.getById(eventId);
        assertEquals(EventStatus.PUBLISHED, publishedEvent.getStatus());
        assertNotNull(publishedEvent.getStartTime());
    }

    @Test
    @Order(2)
    @DisplayName("2. 缓存测试：验证 Caffeine 和 Redis 命中")
    void testCache() {
        // 第一次调用：应该查 DB，并回填缓存
        long start1 = System.currentTimeMillis();
        Event e1 = eventService.getDetail(eventId);
        long time1 = System.currentTimeMillis() - start1;
        log.info("第1次查询耗时 (DB): {}ms", time1);
        assertNotNull(e1);

        // 第二次调用：应该查 Caffeine (极快)
        long start2 = System.currentTimeMillis();
        Event e2 = eventService.getDetail(eventId);
        long time2 = System.currentTimeMillis() - start2;
        log.info("第2次查询耗时 (Cache): {}ms", time2);

        // 断言缓存比 DB 快 (虽不是绝对，但在本地环境通常成立)
        assertTrue(time2 < time1);

        assertEquals(e1.getId(), e2.getId());
    }

    @Test
    @Order(3)
    @DisplayName("3. 下架测试：验证状态变更和缓存失效")
    void testClose() {
        // 下架
        eventService.closeEvent(eventId);

        // 验证 DB 状态
        Event closedEvent = eventService.getById(eventId); // 直接查库，绕过缓存管理器验证
        assertEquals(EventStatus.CLOSED, closedEvent.getStatus());

        // 验证缓存是否已清除并重新加载
        // 如果缓存没清，这里拿到的应该是 PUBLISHED 状态
        Event cachedEvent = eventService.getDetail(eventId);
        assertEquals(EventStatus.CLOSED, cachedEvent.getStatus(), "下架后缓存应该被清除并重新加载为 CLOSED 状态");
    }
}
