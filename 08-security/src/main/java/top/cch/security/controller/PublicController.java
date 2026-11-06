package top.cch.security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.cch.security.common.Result;

import java.util.Map;

/**
 * @author cch
 * @date 2026/10/28
 * @description PublicController 公共接口：连通性检查
 **/
@RestController
@RequestMapping("/public")
public class PublicController {

    @GetMapping("/ping")
    public Result<Map<String, String>> ping() {
        return Result.ok(Map.of("status", "ok"));
    }
}
