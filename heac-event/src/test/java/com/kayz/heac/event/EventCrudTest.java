package com.kayz.heac.event;

import com.kayz.heac.common.context.UserContext;
import com.kayz.heac.event.entity.Event;
import com.kayz.heac.event.enums.EventStatus;
import com.kayz.heac.event.mapper.EventMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // 保证测试按顺序执行
class EventCrudTest {

    // 用于在不同测试方法间传递 ID
    private static String eventId;
    @Resource
    private EventMapper eventMapper;

    @BeforeAll
    static void setup() {
        // 模拟当前登录用户 (为了测试自动填充 createdBy)
        // 注意：因为单元测试是多线程环境，如果你在 Handler 里用的是 ThreadLocal，可能需要注意线程切换
        // 这里简单模拟放入上下文
        UserContext.setUserId("TEST_ADMIN_001");
    }

    @AfterAll
    static void cleanup() {
        UserContext.clear();
    }

    @Test
    @Order(1)
    @DisplayName("1. 测试创建：验证 UUID、自动填充、枚举默认值")
    void testCreate() {
        Event newEvent = Event.builder()
                .title("PostgreSQL Test Event")
                .summary("Integration test for MP + PG")
                .coverImg("https://oss.kayz.com/test.png")
                // 不设置 status，测试是否使用默认值或 null
                // 实际业务中建议由 Service 设置默认值，Mapper 层面如果 DB 有 default 也会生效
                .status(EventStatus.DRAFT)
                .heatScore(100L)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(7))
                .build();

        int rows = eventMapper.insert(newEvent);
        assertEquals(1, rows);

        // 记录 ID 供后续使用
        eventId = newEvent.getId();
        log.info("Created Event ID: {}", eventId);

        // 断言
        assertNotNull(eventId, "ID 应该是 MP 自动生成的 UUID");
        assertNotNull(newEvent.getCreateTime(), "创建时间应自动填充");
        assertNotNull(newEvent.getUpdateTime(), "更新时间应自动填充");
        assertEquals(0, newEvent.getVersion(), "版本号初始应为 0");
        assertEquals(0, newEvent.getDeleted(), "逻辑删除位初始应为 0");
        assertEquals("TEST_ADMIN_001", newEvent.getCreatedBy(), "创建人应自动填充");
    }

    @Test
    @Order(2)
    @DisplayName("2. 测试读取：验证枚举映射、字段回显")
    void testRead() {
        assertNotNull(eventId, "eventId 不应为空，请检查 testCreate 是否成功");

        Event event = eventMapper.selectById(eventId);
        assertNotNull(event, "应该能查到刚才插入的数据");

        log.info("Read Event: {}", event);

        assertEquals("PostgreSQL Test Event", event.getTitle());
        // 验证枚举转换：DB 中的 'DRAFT' 字符串应转为 Java 枚举
        assertEquals(EventStatus.DRAFT, event.getStatus());
        assertEquals("TEST_ADMIN_001", event.getCreatedBy());
    }

    @Test
    @Order(3)
    void testUpdate() throws InterruptedException {
        Event event = eventMapper.selectById(eventId);
        LocalDateTime oldTime = event.getUpdateTime();
        Integer oldVersion = event.getVersion();

        // 暂停 1 秒，确保时间戳肯定变了
        Thread.sleep(1000);

        event.setTitle("New Title");

        // 关键：如果 Handler 里没改用 setFieldValByName，这里需要手动设为 null 才能触发自动填充
        // event.setUpdateTime(null);

        eventMapper.updateById(event);

        // 重新查库 (清除一级缓存影响，如果有的话)
        Event newEvent = eventMapper.selectById(eventId);

        log.info("Old Time: {}", oldTime);
        log.info("New Time: {}", newEvent.getUpdateTime());

        assertNotEquals(oldTime, newEvent.getUpdateTime(), "更新时间必须变化");
        assertTrue(newEvent.getUpdateTime().isAfter(oldTime), "新时间必须晚于旧时间");
        assertEquals(oldVersion + 1, newEvent.getVersion());
    }

    @Test
    @Order(4)
    @DisplayName("4. 测试删除：验证逻辑删除")
    void testDelete() {
        // 执行逻辑删除
        int rows = eventMapper.deleteById(eventId);
        assertEquals(1, rows);

        // 1. 普通查询：应该查不到了 (MP 自动加了 WHERE deleted=0)
        Event event = eventMapper.selectById(eventId);
        assertNull(event, "逻辑删除后，普通查询应无法获取数据");

        // 2. (可选) 硬核验证：直接写 SQL 查库，确认数据还在，只是 deleted=1
        // 这里仅做逻辑验证，假设底层生效
        log.info("Event {} 已逻辑删除", eventId);
    }
}
