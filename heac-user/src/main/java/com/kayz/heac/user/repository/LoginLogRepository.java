package com.kayz.heac.user.repository;

import com.kayz.heac.user.entity.mongo.LoginLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginLogRepository extends MongoRepository<LoginLogDocument, String> {

    // Spring Data 会自动根据方法名生成查询逻辑
    // 相当于: db.login_logs.find({userId: ?}).sort({loginTime: -1}).limit(10)
    List<LoginLogDocument> findTop10ByUserIdOrderByLoginTimeDesc(String userId);
}
