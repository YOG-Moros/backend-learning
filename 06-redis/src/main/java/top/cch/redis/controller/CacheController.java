package top.cch.redis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.cch.redis.dto.CacheResult;
import top.cch.redis.entity.Goods;
import top.cch.redis.service.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/cache/goods")
@RequiredArgsConstructor
public class CacheController {
    private final GoodsService goodsService;
    private final GoodsCacheService manual;
    private final GoodsSpringCacheService annotated;

    @GetMapping("/db/{id}")
    public CacheResult<Goods> database(@PathVariable Long id) {
        long start = System.nanoTime();
        return new CacheResult<>(goodsService.getFromDb(id), "db", (System.nanoTime() - start) / 1_000_000.0);
    }

    @GetMapping("/manual/{id}")
    public CacheResult<Goods> manual(@PathVariable Long id) { return manual.getById(id); }

    @GetMapping("/manual-mutex/{id}")
    public CacheResult<Goods> mutex(@PathVariable Long id) { return manual.getByIdWithMutex(id); }

    @GetMapping("/anno/{id}")
    public CacheResult<Goods> annotated(@PathVariable Long id) {
        long start = System.nanoTime();
        return new CacheResult<>(annotated.getById(id), "spring-cache", (System.nanoTime() - start) / 1_000_000.0);
    }

    @GetMapping("/anno")
    public List<Goods> list() { return annotated.listAll(); }

    @PutMapping("/{id}/price")
    public boolean price(@PathVariable Long id, @RequestParam BigDecimal price) {
        requirePrice(price);
        boolean updated = manual.updatePrice(id, price);
        if (updated) annotated.evictDetailAndList(id);
        return updated;
    }

    @PutMapping("/{id}/price-anno")
    public Goods annotatedPrice(@PathVariable Long id, @RequestParam BigDecimal price) {
        requirePrice(price);
        Goods goods = annotated.updatePrice(id, price);
        manual.evict(id);
        return goods;
    }

    @DeleteMapping("/{id}")
    public void evict(@PathVariable Long id) { manual.evict(id); annotated.evictDetailAndList(id); }

    @DeleteMapping("/anno")
    public void evictAll() { annotated.evictAll(); }

    private void requirePrice(BigDecimal price) {
        if (price.signum() < 0) throw new IllegalArgumentException("价格不能为负数");
    }
}
