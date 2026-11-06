package top.cch.security.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.cch.security.common.Result;
import top.cch.security.dto.LoginRequest;
import top.cch.security.model.LoginUser;
import top.cch.security.service.AuthService;
import top.cch.security.vo.LoginVO;
import top.cch.security.vo.MeVO;

/**
 * @author cch
 * @date 2026/10/28
 * @description AuthController 认证接口：登录换令牌、查当前身份
 *              （退出登录只清前端存储即可，令牌无状态没有后端接口）
 **/
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 登录：匿名可访问，成功返回 JWT */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    /** 查询当前登录身份：需要携带 Bearer 令牌 */
    @GetMapping("/me")
    public Result<MeVO> me(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.ok(authService.me(loginUser));
    }
}
