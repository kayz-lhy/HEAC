package com.kayz.heac.user.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kayz.heac.user.domain.dto.LockUserDTO;
import com.kayz.heac.user.entity.User;
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
    private final UserService userService;

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

            // 写入 Redis 黑名单
            String key = IP_BLACKLIST_KEY + ip;
            if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
                // 封禁 1 小时
                redisTemplate.opsForValue().set(key, "SystemBlock", 1, TimeUnit.HOURS);
                log.warn("【风控触发】封禁异常 IP: {}", ip);
            }
        }
    }

    /**
     * 策略 B: 同一 账号 失败超过 5 次 -> 锁定账号 (调用 UserService)
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

            // 2. 根据账号查询用户实体 (获取 ID)
            User user = userService.getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getAccount, account)
                    .select(User::getId)); // 性能优化：只查 ID 字段即可

            if (user != null) {
                // 3. 组装 DTO
                LockUserDTO lockDTO = new LockUserDTO();
                lockDTO.setUserId(user.getId());
                lockDTO.setReason("系统风控：短时间内多次登录失败");
                lockDTO.setDurationMinutes(60); // 自动锁定 1 小时

                // 4. 调用 Service 执行锁定 (修改 DB状态 + 踢下线)
                try {
                    userService.lockUser(lockDTO);
                } catch (Exception e) {
                    log.error("自动锁定账号失败: {}", account, e);
                }
            } else {
                log.warn("风控扫描发现不存在的账号: {}，可能是恶意随机撞库", account);
            }
        }
    }
}
