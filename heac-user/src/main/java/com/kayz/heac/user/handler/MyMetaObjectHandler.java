package com.kayz.heac.user.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    // 插入时自动填充
    @Override
    public void insertFill(MetaObject metaObject) {
        // setFieldValByName(字段名, 值, MetaObject)
        this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
        // 判断字段为 null 时填充初始版本号
        this.strictInsertFill(metaObject, "version", Integer.class, 1);

        // 判断字段为 null 时填充删除标记
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
        this.strictInsertFill(metaObject, "avatar", String.class, "https://api.dicebear.com/9.x/bottts-neutral/svg?seed=heac-system");
    }

    // 更新自动填充（如果你想对 updateTime 自动填充）
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }
}