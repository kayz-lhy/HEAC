package com.kayz.heac.opinion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@Slf4j
@SpringBootApplication
@EnableFeignClients(basePackages = "com.kayz.heac.common.client")
public class OpinionApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpinionApplication.class, args);
    }
}
