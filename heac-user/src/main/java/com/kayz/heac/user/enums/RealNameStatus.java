package com.kayz.heac.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RealNameStatus {

    /**
     * 未认证
     */
    UNVERIFIED("UNVERIFIED", "未认证"),

    /**
     * 审核中 (如果是人工审核流程)
     */
    PENDING("PENDING", "审核中"),

    /**
     * 已认证 (通过)
     */
    VERIFIED("VERIFIED", "已认证"),

    /**
     * 认证失败 (身份证号错误或人脸不匹配)
     */
    FAILED("FAILED", "认证失败");

    @EnumValue
    private final String code;

    private final String desc;

    @JsonValue
    public String getCode() {
        return code;
    }
}
