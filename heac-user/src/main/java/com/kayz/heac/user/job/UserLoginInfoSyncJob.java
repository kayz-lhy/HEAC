package com.kayz.heac.user.job;

import com.kayz.heac.user.entity.User;
import com.kayz.heac.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.kayz.heac.common.consts.RedisPrefix.LOGIN_IP_CACHE_KEY;
import static com.kayz.heac.common.consts.RedisPrefix.LOGIN_TIME_CACHE_KEY;

/**
 * 用户登录时间同步任务
 * <p>
 * 负责将 Redis 中的登录时间缓冲数据批量写入数据库
 */
@Component
@Slf4j
public class UserLoginInfoSyncJob extends QuartzJobBean {

    private final UserService userService; // 使用 Service 以便利用 MP 的批量更新能力
    private final RedisTemplate<String, Object> redisTemplate;

    public UserLoginInfoSyncJob(UserService userService, RedisTemplate<String, Object> redisTemplate) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // 1. 获取 Redis Hash 中所有数据
        Map<Object, Object> timeEntries = redisTemplate.opsForHash().entries(LOGIN_TIME_CACHE_KEY);
        Map<Object, Object> ipEntries = redisTemplate.opsForHash().entries(LOGIN_IP_CACHE_KEY);
        if (timeEntries.isEmpty() && ipEntries.isEmpty()) {
            return;
        }

        List<User> updateList = new ArrayList<>();
        // 用于记录处理过的 userId，以便后续从 Redis 精确删除，防止误删新数据
        List<Object> processedUserIds = new ArrayList<>();

        // 2. 转换数据
        try {
            timeEntries.forEach((userIdObj, timeStrObj) -> {
                String userId = (String) userIdObj;

                User user = User.builder()
                        .id(userId)
                        .lastLoginTime((LocalDateTime) timeStrObj).build();

                updateList.add(user);
                processedUserIds.add(userId);
            });
            ipEntries.forEach((userIdObj, ipObj) -> {
                String userId = (String) userIdObj;

                User user = updateList.stream()
                        .filter(u -> u.getId().equals(userId))
                        .findFirst()
                        .orElse(User.empty().setId(userId));
                user.setLastLoginIp((String) ipObj);
                updateList.add(user);
                processedUserIds.add(userId);
            });

            // 3. 批量更新到数据库
            // updateBatchById 是 IService 接口提供的方法，内部会分批执行 update
            if (!updateList.isEmpty()) {
                userService.updateBatchById(updateList);

                // 4. 安全清理 Redis
                // 注意：不要直接 delete 整个 Key，因为在执行同步的过程中，可能有新的用户登录并在 Hash 中写入了数据。
                // 我们只删除刚才已经同步到数据库的那些字段。
                redisTemplate.opsForHash().delete(LOGIN_TIME_CACHE_KEY, processedUserIds.toArray());
                redisTemplate.opsForHash().delete(LOGIN_IP_CACHE_KEY, processedUserIds.toArray());

                // 日志记录 (建议使用 Slf4j)
                log.info("Quartz执行用户登录信息同步任务完成：更新了 {} 位用户的登录信息", updateList.size());
            }

        } catch (Exception e) {
            // 实际项目中建议记录 Error 日志
            log.error("Quartz执行用户登录信息同步任务失败", e);
            // 抛出异常，Quartz 根据配置可能会重试
            throw new JobExecutionException(e);
        }
    }
}
