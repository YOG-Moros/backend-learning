package top.cch.actuator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BusinessMetrics {

    private final MeterRegistry registry;

    private final Counter ordersCreated;

    private final AtomicInteger processing = new AtomicInteger();

    public BusinessMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.ordersCreated = Counter.builder("business.orders.created")
                .description("累计创建成功的订单数")
                .register(registry);
        registry.gauge("business.orders.processing", processing);
    }

    public void orderCreated() {
        ordersCreated.increment();
    }

    public void orderFailed(String reason) {
        String label = java.util.Set.of("stock_empty", "pay_timeout", "invalid_request").contains(reason == null ? "" : reason)
                ? reason : "other";
        registry.counter("business.orders.failed", "reason", label).increment();
    }

    public void recordHandle(long millis, String result) {
        Timer.builder("business.order.handle")
                .description("订单处理耗时")
                .tag("result", result)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry)
                .record(Duration.ofMillis(millis));
    }

    public void enter() {
        processing.incrementAndGet();
    }

    public void leave() {
        processing.decrementAndGet();
    }
}
