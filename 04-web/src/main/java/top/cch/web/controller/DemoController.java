package top.cch.web.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.cch.web.common.Result;
import top.cch.web.listener.OnlineCountListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/demo")
public class DemoController {

    @GetMapping("/secure")
    public Result<String> secure() {
        return Result.ok("通过 ApiKeyFilter 校验，已进入控制器");
    }

    @GetMapping("/annotation/hello")
    public Result<String> annotation() {
        return Result.ok("通过 @WebFilter 注解过滤器");
    }

    @GetMapping("/trim")
    public Result<Map<String, Object>> trim(@RequestParam(defaultValue = "") String keyword) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("收到参数", keyword);
        data.put("长度", keyword.length());
        data.put("说明", "ParameterCleanFilter 已去掉两端空格");
        return Result.ok(data);
    }

    @GetMapping("/slow")
    public Result<String> slow(@RequestParam(defaultValue = "200") long ms) throws InterruptedException {
        Thread.sleep(ms);
        return Result.ok("已耗时 " + ms + " ms");
    }

    @GetMapping("/token")
    public Result<String> token() {
        return Result.ok("通过 TokenInterceptor 校验，Controller 正常执行");
    }

    @GetMapping("/callback")
    public Result<String> callback() {
        log.info("Controller 方法执行中");
        return Result.ok("请查看控制台里三个回调的打印顺序");
    }

    @GetMapping("/session")
    public Result<Map<String, Object>> session(HttpSession session) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sessionId", session.getId());
        data.put("在线人数", OnlineCountListener.online());
        return Result.ok(data);
    }
}
