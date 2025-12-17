package com.kayz.heac.opinion.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OpinionStandpoint {

    NEUTRAL(0, "中立"),
    SUPPORT(1, "支持"),
    OPPOSE(2, "反对");

    @EnumValue // 存入数据库的值 (int2)
    @JsonValue // 返回给前端的值 (0, 1, 2)
    private final int code;

    private final String desc;
}
