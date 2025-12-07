package com.kayz.heac.user.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @Size(max = 20, message = "昵称过长")
    private String nickname;

    private String avatar; // 头像URL

    private String bio; // 个人简介

    private String preferences; // 偏好设置 JSON
}

