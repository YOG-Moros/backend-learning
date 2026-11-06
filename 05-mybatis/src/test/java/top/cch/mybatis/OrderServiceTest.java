package top.cch.mybatis;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import top.cch.mybatis.entity.Order;
import top.cch.mybatis.mapper.ProductMapper;
import top.cch.mybatis.service.OrderService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 8.2 练习 5：「创建订单 + 扣库存」事务原子性
 */
@SpringBootTest
class OrderServiceTest {

    @Resource
    private OrderService orderService;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanAndSeed() {
        jdbcTemplate.execute("DELETE FROM t_order");
        jdbcTemplate.execute("DELETE FROM t_product");
        jdbcTemplate.update(
                "INSERT INTO t_product (id, name, stock, price) VALUES (1, '机械键盘', 5, 299.00)");
    }

    @Test
    void createOrderDecrementsStockAndPersistsOrder() {
        Order order = orderService.createOrder(2L, 1L, 3);

        assertNotNull(order.getId());
        assertEquals(new BigDecimal("897.00"), order.getAmount(), "金额 = 单价 299.00 × 3");
        assertEquals(2, productMapper.selectById(1L).getStock(), "库存 5 - 3 = 2");
        // t_order 复用了自动填充与逻辑删除：字段名与 User 相同，同一个 MetaObjectHandler 生效
        assertNotNull(order.getCreateTime());
        assertNotNull(order.getUpdateTime());
        assertEquals(0, order.getDeleted());
        assertEquals(0, order.getVersion());
    }

    @Test
    void insufficientStockRollsBackEverything() {
        // 库存 5，要买 6：扣库存影响 0 行 → 抛异常 → 订单插入一起回滚
        assertThrows(IllegalStateException.class, () -> orderService.createOrder(2L, 1L, 6));
        assertEquals(0, orderService.count(), "订单应整体回滚");
        assertEquals(5, productMapper.selectById(1L).getStock(), "库存不应被扣");
    }

    @Test
    void invalidParamsRejected() {
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(2L, 1L, 0));
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(null, 1L, 1));
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(2L, 999L, 1));
    }
}
