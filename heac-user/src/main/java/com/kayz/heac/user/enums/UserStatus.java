package com.kayz.heac.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {

    /**
     * 正常：可登录，可发言
     */
    NORMAL("NORMAL", "正常"),

    /**
     * 锁定：暂时禁止登录 (通常用于风控，有时效性，如锁定1小时)
     */
    LOCKED("LOCKED", "锁定中"),

    /**
     * 封禁：永久或长期禁止登录 (通常用于人工违规处理)
     */
    BANNED("BANNED", "已封禁"),

    /**
     * 注销：用户主动注销 (逻辑删除的前置状态，数据保留但不可用)
     */
    DELETED("DELETED", "已注销");

    /**
     * 存入数据库的值 (PostgreSQL TEXT)
     */
    @EnumValue
    private final String code;

    /**
     * 前端展示的中文描述
     * 加了 @JsonValue 后，SpringBoot 返回给前端 JSON 时会直接显示这个值
     * (如果你希望前端拿到的是 "NORMAL" 这种 code，就把 @JsonValue 移到 code 上)
     */
    // @JsonValue -> 如果前端需要 "正常"，放这里
    private final String desc;

    // 推荐做法：前端拿 Code，前端自己做国际化映射
    @JsonValue
    public String getCode() {
        return code;
    }
}
