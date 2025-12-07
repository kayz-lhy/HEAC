package com.kayz.heac.user;

import com.kayz.heac.common.exception.AuthException;
import com.kayz.heac.common.exception.UserActionException;
import com.kayz.heac.user.domain.dto.UserLoginDTO;
import com.kayz.heac.user.domain.dto.UserRegisterDTO;
import com.kayz.heac.user.domain.vo.UserLoginVO;
import com.kayz.heac.user.service.AuthService;
import com.kayz.heac.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserActionTest {
    private static UserRegisterDTO dto;
    private final UserService userService;
    private final AuthService authService;
    @Autowired
    RocketMQTemplate rocketMQTemplate;
    @Autowired
    public UserActionTest(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @BeforeAll
    static void setUp() throws UserActionException {
        dto = UserRegisterDTO.builder()
                .account("test-" + System.currentTimeMillis())
                .password("123456")
                .build();
    }

    @Test
    @Order(1)
    void testRegister() throws UserActionException {
        String userId = userService.register(dto);
        assertEquals(32, userId.length());
    }

    @Test
    @Order(2)
    void testLogin() throws UserActionException, AuthException {

        UserLoginDTO loginDto = UserLoginDTO.builder()
                .account(dto.getAccount())
                .password(dto.getPassword())
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        UserLoginVO loginVO = authService.login(loginDto, request);
        assertEquals(dto.getAccount(), loginVO.getAccount());
        assertEquals("USER", loginVO.getRole());
    }


    @Test
    @Order(3)
    void testmq() throws AuthException {
        String msg = "123123123123123123";
        // 不要用 convertAndSend，改用 syncSend 获取结果
        try {
            // 发送同步消息，等待 Broker 确认
            SendResult result = rocketMQTemplate.syncSend("user-topic:login", msg);

            // 打印结果
            log.info("MQ发送状态: {}, MsgID: {}", result.getSendStatus(), result.getMsgId());
            log.info("MQ发送结果: {}", result);
            if (result.getSendStatus() != SendStatus.SEND_OK) {
                log.error("MQ发送失败！");
            }
        } catch (Exception e) {
            log.error("MQ发送极其惨烈地失败了", e);
        }

    }
}
