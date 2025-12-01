package com.kayz.heac.user.config;

import com.kayz.heac.user.job.UserLoginInfoSyncJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    /**
     * 定义 JobDetail
     * 描述任务及其特征
     */
    @Bean
    public JobDetail userLoginTimeSyncJobDetail() {
        return JobBuilder.newJob(UserLoginInfoSyncJob.class)
                .withIdentity("userLoginInfoSyncJob", "userGroup") // 任务名，组名
                .storeDurably() // 即使没有 Trigger 关联，也不删除该 JobDetail
                .withDescription("同步用户最后登录时间从Redis到DB")
                .build();
    }

    /**
     * 定义 Trigger
     * 描述触发规则
     */
    @Bean
    public Trigger userLoginTimeSyncTrigger() {
        // Cron 表达式：每分钟的第 0 秒执行一次
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0,10,20,30,40,50 * * ? * * *");

        return TriggerBuilder.newTrigger()
                .forJob(userLoginTimeSyncJobDetail()) // 关联上面的 JobDetail
                .withIdentity("userLoginTimeSyncTrigger", "userGroup")
                .withSchedule(scheduleBuilder)
                .build();
    }
}
