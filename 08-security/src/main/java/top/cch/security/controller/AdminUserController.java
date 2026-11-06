package top.cch.security.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.cch.security.common.Result;
import top.cch.security.dto.UserCreateRequest;
import top.cch.security.model.LoginUser;
import top.cch.security.service.AdminUserService;
import top.cch.security.vo.UserVO;

import java.util.List;

/**
 * @author cch
 * @date 2026/10/28
 * @description AdminUserController 管理员接口：
 *              URL 规则（/admin/** 要 ROLE_ADMIN）之外，再叠加权限点级注解演示不同粒度
 **/
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /** 账号列表：权限点 user:read */
    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public Result<List<UserVO>> list() {
        return Result.ok(adminUserService.list());
    }

    /** 新增账号：权限点 user:write */
    @PostMapping
    @PreAuthorize("hasAuthority('user:write')")
    public Result<UserVO> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.ok(adminUserService.create(request));
    }

    /** 删除账号：角色级 hasRole（粒度二），与 URL 规则双保险 */
    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@AuthenticationPrincipal LoginUser loginUser,
                               @PathVariable String username) {
        adminUserService.delete(username, loginUser);
        return Result.ok();
    }
}
