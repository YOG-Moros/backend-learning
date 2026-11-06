package top.cch.redis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/redis")
@RequiredArgsConstructor
public class RedisController {

    private final StringRedisTemplate stringRedisTemplate;

    @PostMapping("/counter/incr")
    public Long increment(@RequestParam String key, @RequestParam(defaultValue = "60") long ttlSeconds) {
        checkTtl(ttlSeconds);
        return stringRedisTemplate.execute(top.cch.redis.common.RedisScripts.INCREMENT,
                java.util.List.of(key), String.valueOf(ttlSeconds));
    }

    @PostMapping("/hash")
    public Map<Object, Object> hash(@RequestParam String key, @RequestParam Map<String, String> params,
                                    @RequestParam(defaultValue = "60") long ttlSeconds) {
        checkTtl(ttlSeconds);
        Map<String, String> fields = new LinkedHashMap<>(params);
        fields.remove("key"); fields.remove("ttlSeconds");
        if (fields.isEmpty()) throw new IllegalArgumentException("至少提供一个 Hash 字段");
        stringRedisTemplate.opsForHash().putAll(key, fields);
        stringRedisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        return stringRedisTemplate.opsForHash().entries(key);
    }

    @GetMapping("/hash")
    public Map<Object, Object> getHash(@RequestParam String key) { return stringRedisTemplate.opsForHash().entries(key); }

    @PostMapping("/list")
    public java.util.List<String> list(@RequestParam String key, @RequestParam java.util.List<String> values,
                                      @RequestParam(defaultValue = "60") long ttlSeconds) {
        checkTtl(ttlSeconds);
        if (values.isEmpty()) throw new IllegalArgumentException("values 不能为空");
        stringRedisTemplate.opsForList().rightPushAll(key, values);
        stringRedisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        return stringRedisTemplate.opsForList().range(key, 0, -1);
    }

    @GetMapping("/list")
    public java.util.List<String> getList(@RequestParam String key) { return stringRedisTemplate.opsForList().range(key, 0, -1); }

    @PostMapping("/set")
    public java.util.Set<String> set(@RequestParam String key, @RequestParam java.util.List<String> values,
                                    @RequestParam(defaultValue = "60") long ttlSeconds) {
        checkTtl(ttlSeconds);
        if (values.isEmpty()) throw new IllegalArgumentException("values 不能为空");
        stringRedisTemplate.opsForSet().add(key, values.toArray(String[]::new));
        stringRedisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        return stringRedisTemplate.opsForSet().members(key);
    }

    @GetMapping("/set")
    public java.util.Set<String> getSet(@RequestParam String key) { return stringRedisTemplate.opsForSet().members(key); }

    @GetMapping("/set/intersect")
    public java.util.Set<String> intersect(@RequestParam String key1, @RequestParam String key2) {
        return stringRedisTemplate.opsForSet().intersect(key1, key2);
    }

    @PostMapping("/zset")
    public java.util.Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<String>> zset(
            @RequestParam String key, @RequestParam String member, @RequestParam double score,
            @RequestParam(defaultValue = "60") long ttlSeconds) {
        checkTtl(ttlSeconds);
        if (!Double.isFinite(score)) throw new IllegalArgumentException("score 必须为有限数值");
        stringRedisTemplate.opsForZSet().add(key, member, score);
        stringRedisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        return stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);
    }

    @GetMapping("/zset")
    public java.util.Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<String>> getZset(
            @RequestParam String key, @RequestParam(defaultValue = "5") int top) {
        if (top < 1 || top > 100) throw new IllegalArgumentException("top 范围为 1-100");
        return stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, top - 1);
    }

    private void checkTtl(long ttlSeconds) {
        if (ttlSeconds <= 0) throw new IllegalArgumentException("ttlSeconds 必须大于 0");
    }

    /**
     * String：最基础的类型，文本、数字、序列化后的 JSON 都放它里面
     */
    @PostMapping("/string")
    public Map<String, Object> setString(@RequestParam String key,
                                         @RequestParam String value,
                                         // 默认失效时间为 30 秒
                                         @RequestParam(defaultValue = "30") long ttlSeconds) {
        checkTtl(ttlSeconds);
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("value", value);
        result.put("ttlSeconds", stringRedisTemplate.getExpire(key));
        return result;
    }

    /**
     * 读取 String，key 不存在（已过期或未写入）时返回 null，用来演示自动失效
     */
    @GetMapping("/string")
    public Map<String, Object> getString(@RequestParam String key) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("value", stringRedisTemplate.opsForValue().get(key));
        result.put("ttlSeconds", stringRedisTemplate.getExpire(key));
        return result;
    }
}
