package com.kayz.heac.event.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.kayz.heac.common.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 1. 自动填充时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 2. 自动填充版本号和逻辑删除默认值
        this.strictInsertFill(metaObject, "version", Integer.class, 0);
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);

        // 3. 自动填充创建人 (从 ThreadLocal 获取当前登录用户 ID)
        // 注意：如果是由定时任务触发的插入，UserContext 可能为空，需要做判空处理
        String currentUserId = getUserIdSafe();
        this.strictInsertFill(metaObject, "createdBy", String.class, currentUserId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时只更新 updateTime
//        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }

    private String getUserIdSafe() {
        try {
            // 假设你在 heac-common 中定义了 UserContext
            // 或者是 com.kayz.heac.common.context.UserContext
            String userId = UserContext.getUserId();
            return userId != null ? userId : "SYSTEM";
        } catch (Exception e) {
            return "SYSTEM";
        }
    }
}
