package com.kayz.heac.event.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.kayz.heac.event.entity.Event;
import com.kayz.heac.event.mapper.EventMapper;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static com.kayz.heac.common.consts.RedisPrefix.EVENT_KEY_PREFIX;

@Component
public class EventCacheManager extends AbstractCacheManager<Event> {

    private final EventMapper eventMapper;

    public EventCacheManager(Cache<String, Object> caffeineCache,
                             RedisTemplate<String, Object> redisTemplate,
                             RedissonClient redissonClient,
                             EventMapper eventMapper) {
        super(caffeineCache, redisTemplate, redissonClient);
        this.eventMapper = eventMapper;
    }

    @Override
    protected String getCachePrefix() {
        return EVENT_KEY_PREFIX;
    }

    @Override
    protected Event fetchFromDb(String id) {
        return eventMapper.selectById(id);
    }

    // 如果需要自定义过期时间，可以重写 getRedisTtl
    // @Override
    // protected long getRedisTtl() { return 30; }
}
