package top.cch.redis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.cch.redis.service.StockService;
import java.util.*;

@RestController
@RequestMapping("/lock")
@RequiredArgsConstructor
public class LockController {
    private final StockService stock;

    @PostMapping("/reset")
    public boolean reset(@RequestParam Long id, @RequestParam int stock) { return this.stock.resetStock(id, stock); }

    @GetMapping("/stock/{id}")
    public Map<String, Object> stock(@PathVariable Long id) { return result(id, true, "query"); }

    @PostMapping("/buy/no-lock")
    public Map<String, Object> noLock(@RequestParam Long id, @RequestParam(defaultValue = "1") int quantity) {
        return result(id, stock.deductWithDelayedWindow(id, quantity), "no-lock");
    }

    @PostMapping({"/buy/redisson", "/buy/lock"})
    public Map<String, Object> locked(@RequestParam Long id, @RequestParam(defaultValue = "1") int quantity) {
        return result(id, stock.deductWithLock(id, quantity), "redisson");
    }

    @PostMapping("/buy/setnx")
    public Map<String, Object> setnx(@RequestParam Long id, @RequestParam(defaultValue = "1") int quantity) {
        Boolean success = stock.deductWithSetNx(id, quantity);
        return result(id, Boolean.TRUE.equals(success), success == null ? "setnx-busy" : "setnx");
    }

    private Map<String, Object> result(Long id, boolean success, String mode) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id); result.put("success", success); result.put("mode", mode);
        result.put("stockLeft", stock.getStock(id));
        return result;
    }
}
