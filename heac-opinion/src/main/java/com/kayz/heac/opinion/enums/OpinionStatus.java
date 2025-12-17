package com.kayz.heac.opinion.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OpinionStatus {

    /**
     * 待审核 (刚入库，等待机审或人审)
     */
    PENDING("PENDING", "待审核"),

    /**
     * 已公开 (审核通过，全员可见)
     */
    PUBLISHED("PUBLISHED", "已公开"),

    /**
     * 已驳回 (含敏感词或违规)
     */
    REJECTED("REJECTED", "已驳回"),

    /**
     * 已隐藏 (运营手动折叠)
     */
    HIDDEN("HIDDEN", "已隐藏");

    @EnumValue // 存入数据库的值 (String)
    private final String code;

    @JsonValue // 返回给前端的值
    private final String desc;

    // 如果希望前端拿到的是 "PENDING" 而不是 "待审核"，把 @JsonValue 移到 getCode() 上
    // 推荐做法：
    // public String getCode() { return code; }
}
