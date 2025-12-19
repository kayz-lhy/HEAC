package com.kayz.heac.user.mq;

import com.kayz.heac.common.dto.UserLoginLogDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginLogProducer {
    private final StreamBridge streamBridge;

    public void sendLoginLogMessage(UserLoginLogDTO userLoginLogDTO) {
        streamBridge.send("loginlog-out-0", userLoginLogDTO);
        log.info("登录消息发送");
    }
}
