package com.kayz.heac.user.mq;

import com.kayz.heac.common.dto.UserLoginLogDTO;
import com.kayz.heac.user.entity.mongo.LoginLogDocument;
import com.kayz.heac.user.repository.LoginLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@RocketMQMessageListener(
        topic = "user-topic",
        consumerGroup = "heac-user-login-log-group-dev",
        selectorExpression = "login || login-failure" // 只接收 login 和 login-failure 标签
)
@Component
public class LoginLogConsumer implements RocketMQListener<UserLoginLogDTO> {

    private final LoginLogRepository loginLogRepository;

    public LoginLogConsumer(LoginLogRepository loginLogRepository) {
        this.loginLogRepository = loginLogRepository;
    }

    @Override
    public void onMessage(UserLoginLogDTO msg) {
        try {
            // 1. DTO 转 Document
            LoginLogDocument doc = LoginLogDocument.builder()
                    .userId(msg.getUserId())
                    .account(msg.getAccount())
                    .ip(msg.getIp())
                    .loginTime(msg.getLoginTime()) // 注意时区，推荐使用 UTC 或 LocalDateTime
                    .status(1)
                    .build();

            // 2. 写入 MongoDB (这一步极快)
            loginLogRepository.save(doc);

            log.info("登录日志已写入 MongoDB: {}", doc.getId());

        } catch (Exception e) {
            log.error("写入 MongoDB 失败", e);
            // 抛出异常，RocketMQ 会进行重试
            throw new RuntimeException(e);
        }
    }
}
