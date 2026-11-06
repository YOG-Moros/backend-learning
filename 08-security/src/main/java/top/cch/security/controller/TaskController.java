package top.cch.security.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.cch.security.common.Result;
import top.cch.security.dto.TaskCreateRequest;
import top.cch.security.dto.TaskReviewRequest;
import top.cch.security.model.LoginUser;
import top.cch.security.service.TaskService;
import top.cch.security.vo.TaskVO;

import java.util.List;

/**
 * @author cch
 * @date 2026/10/28
 * @description TaskController 任务接口：
 *              粒度二/三 —— 权限点级 @PreAuthorize；粒度四（数据归属比对）在 Service 层
 **/
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /** 提交任务：需要 task:submit 权限点（学生） */
    @PostMapping
    @PreAuthorize("hasAuthority('task:submit')")
    public Result<TaskVO> submit(@AuthenticationPrincipal LoginUser loginUser,
                                 @Valid @RequestBody TaskCreateRequest request) {
        return Result.ok(taskService.submit(loginUser, request));
    }

    /** 我的提交：登录即可 */
    @GetMapping("/mine")
    public Result<List<TaskVO>> mine(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.ok(taskService.mine(loginUser));
    }

    /** 全班提交列表：需要 task:review 权限点（教师 / 管理员） */
    @GetMapping
    @PreAuthorize("hasAuthority('task:review')")
    public Result<List<TaskVO>> listAll() {
        return Result.ok(taskService.listAll());
    }

    /** 任务详情：本人或持有 task:review，归属比对在 Service（越权 403） */
    @GetMapping("/{id}")
    public Result<TaskVO> detail(@AuthenticationPrincipal LoginUser loginUser,
                                 @PathVariable Long id) {
        return Result.ok(taskService.detail(loginUser, id));
    }

    /** 批改任务：需要 task:review 权限点 */
    @PostMapping("/{id}/review")
    @PreAuthorize("hasAuthority('task:review')")
    public Result<TaskVO> review(@PathVariable Long id,
                                 @Valid @RequestBody TaskReviewRequest request) {
        return Result.ok(taskService.review(id, request));
    }
}
