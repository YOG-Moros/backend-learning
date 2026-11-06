package top.cch.mybatis.service;

import com.baomidou.mybatisplus.spring.service.IService;
import top.cch.mybatis.entity.Order;

public interface OrderService extends IService<Order> {

    /**
     * 8.2 练习 5：创建订单 + 扣库存，两个写操作在同一事务里，要么都成功要么都回滚
     */
    Order createOrder(Long userId, Long productId, Integer quantity);

    boolean cancelOrder(Long id);
}
