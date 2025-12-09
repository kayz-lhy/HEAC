package com.kayz.heac.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录日志查询参数")
public class LoginLogQueryDTO {

    @Schema(description = "页码 (默认1)")
    private int page = 1;

    @Schema(description = "页大小 (默认10)")
    private int size = 10;

    @Schema(description = "用户ID (普通用户查询时会自动覆盖)")
    private String userId;

    @Schema(description = "账号")
    private String account;

    @Schema(description = "IP地址")
    private String ip;

    @Schema(description = "登录状态 (1:成功 0:失败)")
    private Integer status;

    // 可选：增加时间范围查询
    // private LocalDateTime startTime;
    // private LocalDateTime endTime;
}
