package com.kayz.heac.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户登录参数")
public class UserLoginDTO implements Serializable {

    @Schema(description = "账号/手机号", example = "admin")
    @NotBlank(message = "账号不能为空")
    private String account;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;
}
