package top.cch.mybatis.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.cch.mybatis.entity.Order;
import top.cch.mybatis.entity.Product;
import top.cch.mybatis.mapper.OrderMapper;
import top.cch.mybatis.mapper.ProductMapper;
import top.cch.mybatis.service.OrderService;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final ProductMapper productMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Order createOrder(Long userId, Long productId, Integer quantity) {
        if (userId == null || productId == null || quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("userId / productId 必填，quantity 需大于 0");
        }
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在：" + productId);
        }

        // 扣库存：带 stock >= quantity 条件的原子更新，并发下也扣不成负数；
        // 条件不满足影响 0 行，说明库存不足，抛异常让整个事务回滚
        int updated = productMapper.update(null,
                new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, productId)
                        .ge(Product::getStock, quantity)
                        .setSql("stock = stock - " + quantity));
        if (updated == 0) {
            throw new IllegalStateException("库存不足，下单失败：剩余 " + product.getStock() + " < 需要 " + quantity);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        order.setStatus("CREATED");
        save(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long id) {
        Order order = getById(id);
        if (order == null) return false;
        // 条件更新保证并发取消仅有一个请求返还库存。
        boolean changed = update(new LambdaUpdateWrapper<Order>().eq(Order::getId, id)
                .eq(Order::getStatus, "CREATED").set(Order::getStatus, "CANCELLED"));
        if (!changed) return false;
        int restored = productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, order.getProductId()).setSql("stock = stock + {0}", order.getQuantity()));
        if (restored != 1) throw new IllegalStateException("商品不存在，取消订单已回滚");
        return true;
    }
}
