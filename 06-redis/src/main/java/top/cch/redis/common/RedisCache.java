package top.cch.redis.common;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis 读写封装，业务代码不直接操作 RedisTemplate
 *
 * @author cch
 */
@Component
public class RedisCache {

    /**
     * 默认过期时长为 24 小时，单位：秒
     */
    public static final long DEFAULT_EXPIRE = 60 * 60 * 24L;
    /**
     * 不设置过期时长
     */
    public static final long NOT_EXPIRE = -1L;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    public void set(String key, Object value, long expire) {
        if (expire == NOT_EXPIRE) {
            redisTemplate.opsForValue().set(key, value);
        } else {
            // 一条命令带上过期时间，避免 SET 成功后进程退出留下永不过期的 key
            redisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
        }
    }

    public void set(String key, Object value) {
        set(key, value, DEFAULT_EXPIRE);
    }

    /**
     * 不存在才写入，返回是否写入成功。对应 Redis 的 SET key value NX EX expire
     */
    public Boolean setIfAbsent(String key, Object value, long expire) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, expire, TimeUnit.SECONDS);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 自增，并在第一次自增时设置过期时间，用来做固定窗口的计数
     */
    public Long increment(String key, long expire) {
        if (expire == NOT_EXPIRE) return increment(key);
        return stringRedisTemplate.execute(RedisScripts.INCREMENT, java.util.List.of(key), String.valueOf(expire));
    }

    /** 兼容原有 JSON Hash 编码，校验、失败计数与消费在 Redis 内一次完成。 */
    public long consumeSms(String key, String failKey, String code, int maxFailures, long ttl) {
        Long result = stringRedisTemplate.execute(RedisScripts.CONSUME_SMS,
                java.util.List.of(key, failKey), code, String.valueOf(maxFailures), String.valueOf(ttl));
        if (result == null) throw new IllegalStateException("Redis 未返回验证码校验结果");
        return result;
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void delete(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    public Map<String, Object> hGetAll(String key) {
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        return hashOperations.entries(key);
    }

    public void hMSet(String key, Map<String, Object> map, long expire) {
        redisTemplate.opsForHash().putAll(key, map);
        if (expire != NOT_EXPIRE) {
            expire(key, expire);
        }
    }

    public void hSet(String key, String field, Object value, long expire) {
        redisTemplate.opsForHash().put(key, field, value);
        if (expire != NOT_EXPIRE) {
            expire(key, expire);
        }
    }

    public void expire(String key, long expire) {
        redisTemplate.expire(key, expire, TimeUnit.SECONDS);
    }
}
