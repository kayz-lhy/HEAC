package com.kayz.heac.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "封禁用户参数")
public class LockUserDTO implements Serializable {

    @Schema(description = "用户ID")
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @Schema(description = "封禁原因")
    @NotBlank(message = "封禁原因不能为空")
    private String reason;

    @Schema(description = "封禁时长(分钟)，不传则为永久")
    private Integer durationMinutes;
}
