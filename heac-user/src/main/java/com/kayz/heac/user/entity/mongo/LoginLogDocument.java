package com.kayz.heac.user.entity.mongo;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * 登录日志文档
 * collection: 指定 MongoDB 中的集合名（类似表名）
 */
@Data
@Builder
@Document(collection = "login_logs")
public class LoginLogDocument {

    @Id
    private String id; // MongoDB 自动生成的 ObjectId

    @Indexed // 加普通索引，方便按用户查询
    @Field("user_id")
    private String userId;

    @Field("account")
    private String account;

    @Field("ip")
    private String ip;

    @Field("user_agent")
    private String userAgent;

    @Field("status")
    private Integer status; // 1:成功 0:失败

    /**
     * 核心功能：TTL 索引
     * expireAfterSeconds: 指定数据存活秒数
     * 7776000秒 = 90天
     * MongoDB 会自动删除 loginTime 在 90 天前的数据
     */
    @Indexed(expireAfterSeconds = 7776000)
    @Field("login_time")
    private LocalDateTime loginTime;
}
