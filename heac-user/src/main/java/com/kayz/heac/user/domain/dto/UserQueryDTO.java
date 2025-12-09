package com.kayz.heac.user.domain.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class UserQueryDTO {
    private int page = 1;
    private int size = 10;

    private String account;   // 模糊搜
    private String nickname;  // 模糊搜
    private String status;    // 精确搜 (NORMAL, BANNED)

    // 时间范围查询 (前端传 yyyy-MM-dd HH:mm:ss)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeEnd;
}
