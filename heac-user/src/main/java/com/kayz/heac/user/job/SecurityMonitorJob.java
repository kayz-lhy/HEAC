package com.kayz.heac.user.job;

import com.kayz.heac.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.kayz.heac.common.consts.RedisPrefix.IP_BLACKLIST_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityMonitorJob extends QuartzJobBean {

    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserService userService; // 用于修改数据库状态


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("Quartz 任务开始：执行安全风控扫描...");

        // 扫描窗口：最近 2 分钟
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(2);

        // 1. 执行 IP 维度扫描 (封 IP)
        scanAndBanIps(startTime);

        // 2. 执行 账号 维度扫描 (锁账号)
        scanAndLockAccounts(startTime);
    }

    /**
     * 策略 A: 同一 IP 失败超过 5 次 -> 封禁 IP (Redis)
     */
    private void scanAndBanIps(LocalDateTime startTime) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("loginTime").gte(startTime).and("status").is(0)),
                Aggregation.group("ip").count().as("failCount"),
                Aggregation.match(Criteria.where("failCount").gt(5))
        );

        List<Map> results = mongoTemplate.aggregate(agg, "login_logs", Map.class).getMappedResults();

        for (Map map : results) {
            String ip = (String) map.get("_id");
            String key = IP_BLACKLIST_KEY + ip;
            // 封禁 1 小时
            if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
                redisTemplate.opsForValue().set(key, "SystemBlock", 1, TimeUnit.HOURS);
                log.warn("【风控触发】封禁异常 IP: {}", ip);
            }
        }
    }

    /**
     * 策略 B: 同一 账号 失败超过 5 次 -> 锁定账号 (数据库 status)
     */
    private void scanAndLockAccounts(LocalDateTime startTime) {
        // 1. 聚合查询：按 account 分组，统计失败次数
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("loginTime").gte(startTime).and("status").is(0)),
                Aggregation.group("account").count().as("failCount"),
                Aggregation.match(Criteria.where("failCount").gt(5))
        );

        List<Map> results = mongoTemplate.aggregate(agg, "login_logs", Map.class).getMappedResults();

        for (Map map : results) {
            String account = (String) map.get("_id");
            Integer count = (Integer) map.get("failCount");

            log.warn("【风控触发】账号 {} 近期失败 {} 次，准备锁定", account, count);

            // 2. 调用 Service 修改数据库状态
            // 注意：这里需要你自己实现一个根据 account 锁定的方法
            userService.lockUserByAccount(account);
        }
    }
}
