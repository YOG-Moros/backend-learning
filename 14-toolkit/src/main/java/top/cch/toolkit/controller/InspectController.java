package top.cch.toolkit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.cch.toolkit.common.Result;
import top.cch.toolkit.dto.InspectRequest;
import top.cch.toolkit.dto.InspectSummary;
import top.cch.toolkit.service.InspectService;

/**
 * 案例 11：接口巡检
 *
 * @author mqxu
 * @date 2026-10-22
 */
@RestController
@RequestMapping("/toolkit/inspect")
@RequiredArgsConstructor
public class InspectController {

    private final InspectService inspectService;

    @PostMapping
    public Result<InspectSummary> inspect(@RequestBody InspectRequest request) {
        return Result.ok(inspectService.inspect(request.urls()));
    }
}
