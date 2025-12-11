package com.kayz.heac.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kayz.heac.user.domain.dto.LoginLogQueryDTO;
import com.kayz.heac.user.domain.vo.UserLoginLogVO;
import com.kayz.heac.user.entity.mongo.LoginLogDocument;
import com.kayz.heac.user.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<UserLoginLogVO> queryLoginLogs(LoginLogQueryDTO dto) {
        // 1. 构建查询条件
        Query query = new Query();

        // 动态拼接条件 (注意 MongoDB 字段名要和 Document 类里 @Field 对应)
        if (dto.getUserId() != null && !dto.getUserId().isBlank()) {
            query.addCriteria(Criteria.where("user_id").is(dto.getUserId()));
        }
        if (dto.getAccount() != null && !dto.getAccount().isBlank()) {
            query.addCriteria(Criteria.where("account").is(dto.getAccount()));
        }
        if (dto.getIp() != null && !dto.getIp().isBlank()) {
            query.addCriteria(Criteria.where("ip").is(dto.getIp())); // 注意你的字段名是 ip 还是 ip_address
        }
        if (dto.getStatus() != null) {
            query.addCriteria(Criteria.where("status").is(dto.getStatus()));
        }

        // 2. 查询总数 (Count)
        long total = mongoTemplate.count(query, LoginLogDocument.class);

        // 3. 构建空分页 (如果没数据，直接返回，省一次查询)
        Page<UserLoginLogVO> resultPage = new Page<>(dto.getPage(), dto.getSize());
        resultPage.setTotal(total);

        if (total == 0) {
            return resultPage;
        }

        // 4. 查询当前页数据 (Find)
        // page - 1: 因为 Spring Data Mongo 的页码是从 0 开始的，而前端传通常是 1
        PageRequest pageRequest = PageRequest.of(dto.getPage() - 1, dto.getSize(),
                Sort.by(Sort.Direction.DESC, "login_time")); // 默认按时间倒序

        query.with(pageRequest);

        List<LoginLogDocument> documents = mongoTemplate.find(query, LoginLogDocument.class);

        // 5. 转换 Entity -> VO
        List<UserLoginLogVO> vos = documents.stream().map(doc -> {
            UserLoginLogVO vo = new UserLoginLogVO();
            BeanUtils.copyProperties(doc, vo);
            return vo;
        }).collect(Collectors.toList());

        resultPage.setRecords(vos);
        return resultPage;
    }
}
