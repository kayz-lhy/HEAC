package com.kayz.heac.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户列表查询条件")
public class UserQueryDTO implements Serializable {

    @Schema(description = "页码", defaultValue = "1")
    private int page = 1;

    @Schema(description = "每页大小", defaultValue = "10")
    private int size = 10;

    @Schema(description = "账号 (模糊查询)")
    private String account;

    @Schema(description = "昵称 (模糊查询)")
    private String nickname;

    @Schema(description = "状态 (NORMAL/BANNED)")
    private String status;

    @Schema(description = "注册开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeStart;

    @Schema(description = "注册结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeEnd;
}
