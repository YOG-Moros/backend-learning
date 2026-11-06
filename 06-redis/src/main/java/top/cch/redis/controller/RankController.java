package top.cch.redis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;
import top.cch.redis.service.GoodsService;
import java.time.Duration;
import java.util.Set;

@RestController
@RequestMapping("/rank")
@RequiredArgsConstructor
public class RankController {
    private static final String KEY = "rank:goods:sales";
    private final StringRedisTemplate redis;
    private final GoodsService goods;

    @PostMapping("/warmup")
    public Long warmup() {
        redis.delete(KEY);
        goods.listAll().forEach(g -> redis.opsForZSet().add(KEY, g.getId().toString(), g.getSales()));
        redis.expire(KEY, Duration.ofDays(1));
        return redis.opsForZSet().zCard(KEY);
    }

    @PostMapping("/increase")
    public Double increase(@RequestParam Long id, @RequestParam double delta) {
        if (!Double.isFinite(delta)) throw new IllegalArgumentException("delta 必须为有限数值");
        Double score = redis.opsForZSet().incrementScore(KEY, id.toString(), delta);
        redis.expire(KEY, Duration.ofDays(1));
        return score;
    }

    @GetMapping("/top")
    public Set<ZSetOperations.TypedTuple<String>> top(@RequestParam(defaultValue = "5") int top) {
        if (top < 1 || top > 100) throw new IllegalArgumentException("top 范围为 1-100");
        return redis.opsForZSet().reverseRangeWithScores(KEY, 0, top - 1);
    }
}
