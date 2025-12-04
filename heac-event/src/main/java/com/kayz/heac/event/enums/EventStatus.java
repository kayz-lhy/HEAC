package com.kayz.heac.event.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventStatus {

    // @EnumValue: 标记存入数据库的值
    // @JsonValue: 标记前端序列化/反序列化时的值

    DRAFT("DRAFT", "草稿"),
    WARMUP("WARMUP", "预热"),
    PUBLISHED("PUBLISHED", "进行中"),
    ENDED("ENDED", "已结束"),
    CLOSED("CLOSED", "下架");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
