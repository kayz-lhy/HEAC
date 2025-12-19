package com.kayz.heac.user;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.Date;
import java.util.TimeZone;

@SpringBootApplication()
@MapperScan("com.kayz.heac.user.mapper")
@Slf4j
@ComponentScan(basePackages = "com.kayz.heac")
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

    @PostConstruct
    void started() {
        log.info("当前JVM时区: {}, 当前时间: {}", TimeZone.getDefault().getID(), new Date());
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }
}
