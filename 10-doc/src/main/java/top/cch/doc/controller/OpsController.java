package top.cch.doc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.cch.doc.common.Result;

import java.util.Map;

@Tag(name = "运维接口", description = "服务自身的健康与状态信息")
@RestController
@RequestMapping("/ops")
public class OpsController {

    @Operation(summary = "健康检查", description = "返回服务存活状态，供网关与监控探测")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "服务正常")
    })
    @GetMapping("/health")
    public Result<Map<String, String>> health() {
        return Result.ok(Map.of("status", "UP"));
    }
}
