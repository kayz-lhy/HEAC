package com.kayz.heac.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC, staticName = "of")
@NoArgsConstructor(access = AccessLevel.PUBLIC, staticName = "empty")
public class UserRegisterDTO implements Serializable {
    @NotBlank(message = "账号不能为空")
    @Size(min = 6, max = 20, message = "账号长度不能小于6,不能大于20")
    private String account;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度不能小于6,不能大于20")
    private String password;
}