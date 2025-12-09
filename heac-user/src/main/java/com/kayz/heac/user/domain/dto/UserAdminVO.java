package com.kayz.heac.user.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAdminVO {
    private String id;
    private String account;
    private String nickname;
    private String avatar;
    private String status;
    private String realNameStatus;

    // --- 下面是管理员特权可见字段 ---
    private String lastLoginIp;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    // 甚至可以包含真实姓名 (脱敏后)
    // private String realNameMasked; 
}
