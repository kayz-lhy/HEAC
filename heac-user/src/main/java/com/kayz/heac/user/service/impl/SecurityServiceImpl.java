package com.kayz.heac.user.service.impl;

import com.kayz.heac.user.domain.vo.UserLoginLogVO;
import com.kayz.heac.user.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {
    private final MongoTemplate mongoTemplate;

    @Override
    public List<UserLoginLogVO> getLoginLogs(int pageNum, int pageSize, String userId, String account, String ip, Integer status) {
        Query query = new Query().with(PageRequest.of(pageNum, pageSize));
        if (userId != null) query.addCriteria(Criteria.where("user_id").is(userId));
        if (account != null) query.addCriteria(Criteria.where("account").is(account));
        if (ip != null) query.addCriteria(Criteria.where("ip_address").is(ip));
        if (status != null) query.addCriteria(Criteria.where("status").is(status));
        return mongoTemplate.find(query, UserLoginLogVO.class, "login_logs");
    }
}
