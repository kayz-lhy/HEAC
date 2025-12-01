package com.kayz.heac.user;

import com.kayz.heac.common.dto.UserLoginDTO;
import com.kayz.heac.common.dto.UserLoginVO;
import com.kayz.heac.common.dto.UserRegisterDTO;
import com.kayz.heac.common.exception.AuthException;
import com.kayz.heac.common.exception.UserActionException;
import com.kayz.heac.user.service.AuthService;
import com.kayz.heac.user.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserActionTest {
    private static UserRegisterDTO dto;
    private final UserService userService;
    private final AuthService authService;

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
}
