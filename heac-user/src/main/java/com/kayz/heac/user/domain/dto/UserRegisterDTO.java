package com.kayz.heac.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Data
@Schema(description = "用户注册参数")
public class UserRegisterDTO implements Serializable {

    @Schema(description = "账号 (字母开头，允许数字下划线)", example = "kayz_007")
    @NotBlank(message = "账号不能为空")
    @Length(min = 4, max = 20, message = "账号长度需在4-20位之间")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "账号格式不合法")
    private String account;

    @Schema(description = "密码 (建议复杂组合)", example = "Pwd@1234")
    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度需在6-20位之间")
    private String password;
}
