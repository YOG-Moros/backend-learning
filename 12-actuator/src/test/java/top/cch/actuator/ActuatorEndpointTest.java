package top.cch.actuator;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("监控端点")
class ActuatorEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry registry;

    @Test
    @DisplayName("健康检查里有自定义的 inventory 组件与判断依据")
    void healthHasInventoryComponent() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.inventory.status").value("UP"))
                .andExpect(jsonPath("$.components.inventory.details.url").exists())
                .andExpect(jsonPath("$.components.ping.status").value("UP"));
    }

    @Test
    @DisplayName("info 端点带上 InfoContributor 写的信息")
    void infoHasCustomDetails() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.module.name").value("13-应用监控"))
                .andExpect(jsonPath("$.owner.team").value("backend-learning"));
    }

    @Test
    @DisplayName("打三次下单，计数器正好加三")
    void counterGrowsByThree() throws Exception {
        double before = registry.get("business.orders.created").counter().count();

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/business/orders").param("amount", "100"))
                    .andExpect(status().isOk());
        }

        double after = registry.get("business.orders.created").counter().count();
        assertThat(after - before).isEqualTo(3.0);
    }

    @Test
    @DisplayName("metrics 端点按名字能查到单个指标")
    void metricByName() throws Exception {
        mockMvc.perform(post("/business/orders")).andExpect(status().isOk());

        mockMvc.perform(get("/actuator/metrics/business.orders.created"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("business.orders.created"))
                .andExpect(jsonPath("$.measurements[0].statistic").value("COUNT"))
                .andExpect(jsonPath("$.measurements[0].value").isNumber());
    }

    @Test
    @DisplayName("metrics 清单里能看到四个自定义指标")
    void metricListContainsBusinessMetrics() throws Exception {
        mockMvc.perform(get("/business/fail").param("reason", "stock_empty"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray())
                .andExpect(content().string(containsString("business.orders.created")))
                .andExpect(content().string(containsString("business.order.handle")))
                .andExpect(content().string(containsString("business.orders.failed")))
                .andExpect(content().string(containsString("business.orders.processing")));
    }

    @Test
    @DisplayName("prometheus 端点输出文本格式，计数器名带 _total")
    void prometheusTextFormat() throws Exception {
        mockMvc.perform(post("/business/orders")).andExpect(status().isOk());

        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("# TYPE business_orders_total counter")))
                .andExpect(content().string(containsString("business_orders_total{")))
                .andExpect(content().string(containsString("# TYPE business_orders_processing gauge")));
    }

    @Test
    @DisplayName("没写进 exposure.include 的端点访问不到")
    void unexposedEndpointIs404() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isNotFound());
    }
}
