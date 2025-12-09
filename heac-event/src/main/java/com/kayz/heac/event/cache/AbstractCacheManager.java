package com.kayz.heac.event.cache;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractCacheManager<T> implements EntityCacheManager<T> {
    private final Cache<String, Object> caffeineCache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient; // 注入 Redisson 客户端

    protected AbstractCacheManager(Cache<String, Object> caffeineCache, RedisTemplate<String, Object> redisTemplate, RedissonClient redissonClient) {
        this.caffeineCache = caffeineCache;
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
    }


    // --- 抽象方法 ---
    protected abstract String getCachePrefix();

    protected abstract T fetchFromDb(String id);

    protected long getRedisTtl() {
        return 10;
    }

    protected TimeUnit getRedisTtlUnit() {
        return TimeUnit.MINUTES;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(String id) {
        String cacheKey = getCachePrefix() + id;

        // 1. 第一次查缓存 (Caffeine + Redis)
        T cacheResult = getFromCache(cacheKey);
        if (cacheResult != null) {
            return cacheResult;
        }

        // 2. 缓存未命中，准备回源数据库
        // 定义锁的 Key (例如: lock:heac:event:detail:123)
        // 加上 "lock:" 前缀防止和真实数据 Key 冲突
        String lockKey = "lock:" + cacheKey;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 3. 尝试获取锁
            // tryLock(等待时间, 锁自动释放时间, 单位)
            // 等待 500ms: 如果别人正在查库，我等一会
            // 自动释放 5s: 防止节点挂掉死锁，数据库查询一般不会超过 5s
            boolean isLocked = lock.tryLock(500, 5000, TimeUnit.MILLISECONDS);

            if (isLocked) {
                try {
                    // 4. 【关键】双重检查 (Double Check)
                    // 在我等待锁的时候，可能前一个线程已经把数据查回来放缓存了
                    // 所以拿到锁之后，必须再查一次缓存
                    T doubleCheck = getFromCache(cacheKey);
                    if (doubleCheck != null) {
                        log.debug("Double Check Hit: {}", cacheKey);
                        return doubleCheck;
                    }

                    // 5. 确实没有，查询数据库
                    log.info("Cache MISS, Query DB: {}", cacheKey);
                    T dbObj = fetchFromDb(id);

                    if (dbObj != null) {
                        // 6. 写入缓存
                        redisTemplate.opsForValue().set(cacheKey, dbObj, getRedisTtl(), getRedisTtlUnit());
                        caffeineCache.put(cacheKey, dbObj);
                    } else {
                        // 7. (可选) 缓存空值，防止缓存穿透
                        //redisTemplate.opsForValue().set(cacheKey, NullObject, 1, TimeUnit.MINUTES);
                    }
                    return dbObj;
                } finally {
                    lock.unlock(); // 释放锁
                }
            } else {
                // 8. 获取锁失败 (说明并发非常高，别人正在查库)
                // 策略：等待一会儿再重试查询缓存，或者直接返回失败
                // 这里简单处理：休息 200ms 后递归调用自己，或者直接返回 null 让前端重试
                Thread.sleep(200);
                return get(id); // 递归重试
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock interrupted", e);
            return null;
        } catch (Exception e) {
            log.error("Cache Get Error", e);
            return null;
        }
    }

    /**
     * 提取公共的查缓存逻辑
     */
    @SuppressWarnings("unchecked")
    private T getFromCache(String key) {
        // 查一级
        T localObj = (T) caffeineCache.getIfPresent(key);
        if (localObj != null) return localObj;

        // 查二级
        Object redisObj = redisTemplate.opsForValue().get(key);
        if (redisObj != null) {
            T entity = (T) redisObj;
            caffeineCache.put(key, entity); // 回填一级
            return entity;
        }
        return null;
    }

    @Override
    public void invalidate(String id) {
        String key = getCachePrefix() + id;
        redisTemplate.delete(key);
        caffeineCache.invalidate(key);
    }
}
