package com.kayz.heac.user.mq;

import com.kayz.heac.common.dto.UserLoginLogDTO;
import com.kayz.heac.user.entity.mongo.LoginLogDocument;
import com.kayz.heac.user.repository.LoginLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class LoginLogConsumerX {

    /**
     * 定义消费者函数
     * 方法名 loginLogConsume 必须与 yml 中 function.definition 一致
     */
    @Bean
    public Consumer<UserLoginLogDTO> loginLogConsumer(LoginLogRepository loginLogRepository) {
        return msg -> {
            log.info("收到登录日志: userId={}", msg.getUserId());

            try {
                // 转 Mongo Document
                LoginLogDocument doc = LoginLogDocument.builder()
                        .userId(msg.getUserId())
                        .account(msg.getAccount())
                        .ip(msg.getIp())
                        .loginTime(msg.getLoginTime())
                        .status(1)
                        .build();

                // 写入 MongoDB
                loginLogRepository.save(doc);

            } catch (Exception e) {
                log.error("写入 Mongo 失败", e);
                throw new RuntimeException(e); // 抛异常触发重试
            }
        };
    }
}
