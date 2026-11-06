package top.cch.actuator.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.cch.actuator.common.Result;
import top.cch.actuator.metrics.BusinessMetrics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequestMapping("/business")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessMetrics metrics;

    private final AtomicLong idGenerator = new AtomicLong();

    @PostMapping("/orders")
    public Result<Map<String, Object>> create(@RequestParam(defaultValue = "100") long amount) {
        long start = System.currentTimeMillis();
        metrics.enter();
        try {
            long id = idGenerator.incrementAndGet();
            metrics.orderCreated();
            metrics.recordHandle(System.currentTimeMillis() - start, "success");

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("orderId", id);
            data.put("amount", amount);
            data.put("costMs", System.currentTimeMillis() - start);
            return Result.ok(data);
        } finally {
            metrics.leave();
        }
    }

    @GetMapping("/slow")
    public Result<Map<String, Object>> slow(@RequestParam(defaultValue = "200") long ms) throws InterruptedException {
        long start = System.currentTimeMillis();
        if (ms < 0 || ms > 30000) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST);
        metrics.enter();
        try {
            Thread.sleep(ms);
            metrics.recordHandle(System.currentTimeMillis() - start, "success");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            metrics.recordHandle(System.currentTimeMillis() - start, "failure");
            throw e;
        } finally { metrics.leave(); }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sleepMs", ms);
        data.put("note", "到 /actuator/metrics/business.order.handle 看 P95 与最大值");
        return Result.ok(data);
    }

    @GetMapping("/fail")
    public Result<Map<String, Object>> fail(@RequestParam(defaultValue = "stock_empty") String reason) {
        long start = System.currentTimeMillis();
        metrics.orderFailed(reason);
        metrics.recordHandle(System.currentTimeMillis() - start, "failure");
        log.warn("模拟下单失败，原因：{}", reason);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reason", reason);
        data.put("note", "到 /actuator/metrics/business.orders.failed 看各原因的计数");
        return Result.ok(data);
    }

    @GetMapping("/hello")
    public Result<Map<String, Object>> hello() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("module", "13-actuator");
        data.put("health", "/actuator/health");
        data.put("metrics", "/actuator/metrics");
        data.put("prometheus", "/actuator/prometheus");
        return Result.ok(data);
    }
}
