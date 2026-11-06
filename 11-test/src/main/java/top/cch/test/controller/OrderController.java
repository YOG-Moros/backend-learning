package top.cch.test.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.cch.test.common.Result;
import top.cch.test.dto.OrderRequest;
import top.cch.test.dto.OrderResult;
import top.cch.test.service.OrderService;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/calculate")
    public Result<OrderResult> calculate(@Valid @RequestBody OrderRequest request) {
        return Result.ok(orderService.calculate(request));
    }

    @GetMapping("/quick-calculate")
    public Result<OrderResult> quickCalculate(@RequestParam BigDecimal amount,
                                             @RequestParam(defaultValue = "NORMAL") String memberLevel) {
        return Result.ok(orderService.calculate(new OrderRequest(1L, amount, memberLevel, 1)));
    }

    @GetMapping("/hello")
    public Result<Map<String, Object>> hello() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("module", "12-test");
        data.put("hint", "执行 mvn -pl 12-test test 可以跑全部测试用例");
        data.put("coverage", "target/site/jacoco/index.html");
        return Result.ok(data);
    }
}
