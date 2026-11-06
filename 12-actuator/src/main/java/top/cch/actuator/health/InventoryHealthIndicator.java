package top.cch.actuator.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("inventory")
public class InventoryHealthIndicator implements HealthIndicator {

    @Value("${demo.inventory.healthy:true}")
    private boolean healthy;

    @Value("${demo.inventory.url:http://inventory-service/health}")
    private String url;

    @Override
    public Health health() {
        long start = System.currentTimeMillis();
        if (!healthy) {
            return Health.down()
                    .withDetail("url", url)
                    .withDetail("reason", "连接超时")
                    .build();
        }
        long cost = System.currentTimeMillis() - start;
        return Health.up()
                .withDetail("url", url)
                .withDetail("latencyMs", cost)
                .withDetail("checkedAt", System.currentTimeMillis())
                .build();
    }
}
