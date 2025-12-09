package com.kayz.heac.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LockUserDTO {
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @NotBlank(message = "封禁原因不能为空")
    private String reason;

    // 可选：封禁时长 (分钟)，null 表示永久
    private Integer durationMinutes;
}
