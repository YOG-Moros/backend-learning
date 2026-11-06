package top.cch.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import top.cch.redis.entity.Goods;
import top.cch.redis.mapper.GoodsMapper;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodsSpringCacheService {

    private final GoodsMapper goodsMapper;

    @Cacheable(cacheNames = "goods", key = "#id")
    public Goods getById(Long id) {
        return goodsMapper.selectById(id);
    }

    @Cacheable(cacheNames = "goods", key = "'all'")
    public List<Goods> listAll() {
        return goodsMapper.selectList(null);
    }

    @CachePut(cacheNames = "goods", key = "#id")
    @CacheEvict(cacheNames = "goods", key = "'all'")
    public Goods updatePrice(Long id, BigDecimal price) {
        Goods update = new Goods();
        update.setId(id);
        update.setPrice(price);
        if (goodsMapper.updateById(update) == 0) {
            return null;
        }
        return goodsMapper.selectById(id);
    }

    @CacheEvict(cacheNames = "goods", key = "#id")
    public void evict(Long id) {
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "goods", key = "#id"),
            @CacheEvict(cacheNames = "goods", key = "'all'")
    })
    public void evictDetailAndList(Long id) {
    }

    @CacheEvict(cacheNames = "goods", allEntries = true)
    public void evictAll() {
    }
}
