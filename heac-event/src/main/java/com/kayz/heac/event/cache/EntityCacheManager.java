package com.kayz.heac.event.cache;

/**
 * 通用实体缓存管理器接口
 *
 * @param <T> 实体类型
 */
public interface EntityCacheManager<T> {

    /**
     * 获取详情 (Caffeine -> Redis -> DB)
     *
     * @param id 主键ID (UUID String)
     * @return 实体对象
     */
    T get(String id);

    /**
     * 失效缓存 (删除 Redis 和 Caffeine)
     *
     * @param id 主键ID
     */
    void invalidate(String id);
}

