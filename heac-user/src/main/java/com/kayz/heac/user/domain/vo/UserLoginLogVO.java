package com.kayz.heac.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserLoginLogVO {
    private String id;        // Mongo ID
    private String account;
    private String ip;
    private Integer status;

    // 注意：MongoDB 存的时间是 UTC，Spring 转出来可能是 LocalDateTime
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
}
