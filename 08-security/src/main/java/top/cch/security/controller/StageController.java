package top.cch.security.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import top.cch.security.common.Result;
import java.util.Map;

@RestController
public class StageController {
    @GetMapping({"/stage1/hello", "/stage2/hello", "/stage3/hello", "/stage4/me", "/api/hello", "/api/me"})
    public Result<Map<String, Object>> hello(Authentication authentication) {
        return Result.ok(Map.of("message", "你好，" + authentication.getName(), "username", authentication.getName(),
                "authorities", authentication.getAuthorities().stream().map(a -> a.getAuthority()).toList()));
    }
    @GetMapping({"/stage2/public/hello", "/api/public/hello"})
    public Result<String> publicHello() { return Result.ok("公开接口"); }
    @GetMapping("/stage2/articles/{id}")
    public Result<Map<String, Long>> article(@PathVariable Long id) { return Result.ok(Map.of("id", id)); }
    @DeleteMapping({"/stage2/articles/{id}", "/stage3/articles/{id}", "/api/articles/{id}"})
    public Result<Map<String, Long>> delete(@PathVariable Long id) { return Result.ok(Map.of("deleted", id)); }
    @GetMapping({"/stage3/admin/dashboard", "/api/admin/dashboard"})
    public Result<String> admin() { return Result.ok("管理员面板"); }
    @GetMapping({"/stage3/ops/status", "/api/ops/status"})
    public Result<String> ops() { return Result.ok("运维状态"); }
    @GetMapping({"/stage3/audit/logs", "/api/audit/logs"})
    public Result<String> audit() { return Result.ok("审计日志"); }
    @GetMapping("/stage4/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "parameterName", token.getParameterName(), "headerName", token.getHeaderName());
    }
}
