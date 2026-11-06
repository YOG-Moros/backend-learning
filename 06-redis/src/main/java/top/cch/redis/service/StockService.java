package top.cch.redis.service;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import top.cch.redis.config.DemoProperties;
import top.cch.redis.entity.Goods;
import top.cch.redis.mapper.GoodsMapper;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class StockService {

    private static final String LOCK_PREFIX = "lock:goods:stock:";

    private final GoodsMapper goodsMapper;
    private final RedissonClient redissonClient;
    private final DemoProperties properties;
    private final org.springframework.data.redis.core.StringRedisTemplate redis;

    /** 返回 null 表示锁忙；false 表示库存不足。 */
    public Boolean deductWithSetNx(Long id, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity 必须大于 0");
        String key = "lock:goods:setnx:" + id;
        String owner = java.util.UUID.randomUUID().toString();
        if (!Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key, owner, java.time.Duration.ofSeconds(10)))) return null;
        try { return deductAtomically(id, quantity); }
        finally { redis.execute(top.cch.redis.common.RedisScripts.UNLOCK, java.util.List.of(key), owner); }
    }

    private boolean deductAtomically(Long id, int quantity) {
        return goodsMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Goods>()
                .eq(Goods::getId, id).ge(Goods::getStock, quantity).setSql("stock = stock - {0}", quantity)) > 0;
    }

    public boolean deduct(Long id, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity 必须大于 0");
        Goods goods = goodsMapper.selectById(id);
        if (goods == null || goods.getStock() < quantity) {
            return false;
        }
        Goods update = new Goods();
        update.setId(id);
        update.setStock(goods.getStock() - quantity);
        return goodsMapper.updateById(update) > 0;
    }

    public boolean deductWithDelayedWindow(Long id, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity 必须大于 0");
        Goods goods = goodsMapper.selectById(id);
        if (goods == null || goods.getStock() < quantity) {
            return false;
        }
        sleepQuietly(20);
        Goods update = new Goods();
        update.setId(id);
        update.setStock(goods.getStock() - quantity);
        return goodsMapper.updateById(update) > 0;
    }

    public boolean deductWithLock(Long id, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity 必须大于 0");
        RLock lock = redissonClient.getLock(LOCK_PREFIX + id);
        try {
            boolean locked = lock.tryLock(properties.getLock().getWaitSeconds(),
                    properties.getLock().getLeaseSeconds(), TimeUnit.SECONDS);
            if (!locked) {
                return false;
            }
            return deductAtomically(id, quantity);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public boolean resetStock(Long id, int stock) {
        if (stock < 0) throw new IllegalArgumentException("stock 不能为负数");
        Goods update = new Goods();
        update.setId(id);
        update.setStock(stock);
        return goodsMapper.updateById(update) > 0;
    }

    public Integer getStock(Long id) {
        Goods goods = goodsMapper.selectById(id);
        return goods == null ? null : goods.getStock();
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
