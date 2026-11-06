package top.cch.redis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.cch.redis.service.RateLimitService;
import java.util.Map;

@RestController
@RequestMapping("/limit")
@RequiredArgsConstructor
public class LimitController {
    private final RateLimitService service;
    @GetMapping("/access")
    public Map<String, Object> access(@RequestParam(defaultValue = "guest") String user) {
        return service.access(user);
    }
}
