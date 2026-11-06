package top.cch.mybatis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.cch.mybatis.entity.Order;
import top.cch.mybatis.service.OrderService;

import java.util.List;

/**
 * 8.2 练习 5：订单接口，「创建订单 + 扣库存」事务原子性演示
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** 下单成功返回订单；库存不足抛异常（500），订单和库存都不变 */
    @PostMapping
    public Order create(@RequestParam Long userId,
                        @RequestParam Long productId,
                        @RequestParam Integer quantity) {
        return orderService.createOrder(userId, productId, quantity);
    }

    @GetMapping
    public List<Order> list(@RequestParam(required = false) Long userId) {
        return orderService.lambdaQuery().eq(userId != null, Order::getUserId, userId).list();
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}/cancel")
    public boolean cancel(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public boolean delete(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return orderService.removeById(id);
    }
}
