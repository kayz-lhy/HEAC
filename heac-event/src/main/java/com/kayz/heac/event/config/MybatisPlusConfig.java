package com.kayz.heac.event.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 分页插件 (指定数据库为 PostgreSQL)
//        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));

        // 2. 乐观锁插件 (配合 @Version 注解)
        // 解决并发更新问题：UPDATE ... SET version = 2 WHERE id = 1 AND version = 1
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 3. 防全表更新/删除插件 (安全防护)
//        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }
}
