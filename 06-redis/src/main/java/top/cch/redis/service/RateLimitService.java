package top.cch.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.cch.redis.common.RedisScripts;
import top.cch.redis.config.DemoProperties;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final StringRedisTemplate redis;
    private final DemoProperties properties;

    public Map<String, Object> access(String user) {
        int window = properties.getLimit().getWindowSeconds();
        int max = properties.getLimit().getMaxRequests();
        if (window <= 0 || max <= 0) throw new IllegalStateException("限流窗口和次数必须为正数");
        String key = "limit:access:" + user + ":" + System.currentTimeMillis() / 1000 / window;
        Long count = redis.execute(RedisScripts.INCREMENT, List.of(key), String.valueOf(window));
        long actual = count == null ? max + 1L : count;
        return Map.of("user", user, "count", actual, "maxRequests", max,
                "remaining", Math.max(0, max - actual), "allowed", actual <= max,
                "ttlSeconds", redis.getExpire(key));
    }
}
