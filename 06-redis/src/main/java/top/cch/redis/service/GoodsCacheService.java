package top.cch.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.cch.redis.config.DemoProperties;
import top.cch.redis.dto.CacheResult;
import top.cch.redis.entity.Goods;
import top.cch.redis.mapper.GoodsMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class GoodsCacheService {

    private static final String KEY_PREFIX = "goods:detail:";
    private static final String NULL_PLACEHOLDER = "NULL";
    private static final String REBUILD_LOCK_PREFIX = "lock:goods:rebuild:";

    private final GoodsMapper goodsMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final DemoProperties properties;

    public CacheResult<Goods> getById(Long id) {
        long start = System.nanoTime();
        String key = KEY_PREFIX + id;

        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return fromCache(cached, start);
        }

        Goods goods = goodsMapper.selectById(id);
        cacheIt(key, goods);
        return new CacheResult<>(goods, "db", costMs(start));
    }

    public CacheResult<Goods> getByIdWithMutex(Long id) {
        long start = System.nanoTime();
        String key = KEY_PREFIX + id;

        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return fromCache(cached, start);
        }

        String lockKey = REBUILD_LOCK_PREFIX + id;
        String owner = java.util.UUID.randomUUID().toString();
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, owner, Duration.ofSeconds(5));
        if (!Boolean.TRUE.equals(locked)) {
            sleepQuietly(80);
            Object retried = redisTemplate.opsForValue().get(key);
            if (retried != null) {
                return fromCache(retried, start);
            }
            return new CacheResult<>(goodsMapper.selectById(id), "db-fallback", costMs(start));
        }

        try {
            Object checked = redisTemplate.opsForValue().get(key);
            if (checked != null) {
                return fromCache(checked, start);
            }
            Goods goods = goodsMapper.selectById(id);
            cacheIt(key, goods);
            return new CacheResult<>(goods, "db", costMs(start));
        } finally {
            stringRedisTemplate.execute(top.cch.redis.common.RedisScripts.UNLOCK, java.util.List.of(lockKey), owner);
        }
    }

    public void cacheIt(String key, Goods goods) {
        if (goods == null) {
            redisTemplate.opsForValue()
                    .set(key, NULL_PLACEHOLDER, properties.getCache().getNullTtl());
            return;
        }
        redisTemplate.opsForValue().set(key, goods, ttlWithJitter());
    }

    public Duration ttlWithJitter() {
        long base = properties.getCache().getTtl().toSeconds();
        long jitter = ThreadLocalRandom.current()
                .nextLong(properties.getCache().getJitterSeconds() + 1L);
        return Duration.ofSeconds(base + jitter);
    }

    public boolean updatePrice(Long id, BigDecimal price) {
        Goods goods = new Goods();
        goods.setId(id);
        goods.setPrice(price);
        boolean updated = goodsMapper.updateById(goods) > 0;
        if (updated) {
            evict(id);
        }
        return updated;
    }

    public void evict(Long id) {
        redisTemplate.delete(KEY_PREFIX + id);
    }

    private CacheResult<Goods> fromCache(Object cached, long start) {
        if (NULL_PLACEHOLDER.equals(cached)) {
            return new CacheResult<>(null, "cache-empty", costMs(start));
        }
        return new CacheResult<>((Goods) cached, "cache", costMs(start));
    }

    private double costMs(long start) {
        return (System.nanoTime() - start) / 1_000_000.0;
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
