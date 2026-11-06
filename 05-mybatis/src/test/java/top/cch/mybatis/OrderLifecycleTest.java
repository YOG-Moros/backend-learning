package top.cch.mybatis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import top.cch.mybatis.mapper.ProductMapper;
import top.cch.mybatis.service.OrderService;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderLifecycleTest {
    @Autowired OrderService orders;
    @Autowired ProductMapper products;
    @Test void cancellationRestoresStockOnlyOnceAndDeleteIsLogical() {
        int before=products.selectById(1L).getStock();
        var order=orders.createOrder(1L,1L,1);
        assertThat(products.selectById(1L).getStock()).isEqualTo(before-1);
        assertThat(orders.cancelOrder(order.getId())).isTrue();
        assertThat(orders.cancelOrder(order.getId())).isFalse();
        assertThat(products.selectById(1L).getStock()).isEqualTo(before);
        assertThat(orders.getById(order.getId()).getStatus()).isEqualTo("CANCELLED");
        assertThat(orders.removeById(order.getId())).isTrue();
        assertThat(orders.getById(order.getId())).isNull();
    }
}
