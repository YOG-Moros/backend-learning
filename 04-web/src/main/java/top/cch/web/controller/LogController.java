package top.cch.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.cch.web.common.Result;

@Slf4j
@RestController
@RequestMapping("/api/log")
public class LogController {

    @GetMapping("/hello")
    public Result<String> hello(@RequestParam(required = false, defaultValue = "world") String name,
                                @RequestParam(required = false, defaultValue = "18") Integer age) throws InterruptedException {
        Thread.sleep(1000);
        log.info("【Controller 执行】参数：{}，{}（能打印说明过滤器和 preHandle 都已放行）", name, age);
        return Result.ok("hello, " + name + ", age=" + age);
    }
}
