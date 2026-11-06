package top.cch.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.cch.redis.entity.Goods;
import top.cch.redis.mapper.GoodsMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodsService {

    private final GoodsMapper goodsMapper;

    public List<Goods> listAll() {
        return goodsMapper.selectList(null);
    }

    public Goods getFromDb(Long id) {
        return goodsMapper.selectById(id);
    }
}
