package com.kayz.heac.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Data
@Schema(description = "用户资料更新参数")
public class UserUpdateDTO implements Serializable {

    @Schema(description = "新昵称")
    @Length(max = 20, message = "昵称最长20个字符")
    private String nickname;

    @Schema(description = "新头像URL")
    private String avatar;

    @Schema(description = "个人简介")
    @Length(max = 200, message = "简介最长200个字符")
    private String bio;

    @Schema(description = "偏好设置JSON字符串")
    private String preferences;
}
