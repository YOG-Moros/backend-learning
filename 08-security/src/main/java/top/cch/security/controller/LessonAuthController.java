package top.cch.security.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import top.cch.security.common.Result;
import top.cch.security.config.SecurityProperties;
import top.cch.security.dto.LoginRequest;
import top.cch.security.security.JwtService;
import top.cch.security.sms.*;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class LessonAuthController {
    public record SmsLoginRequest(@NotBlank @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone,
                                  @NotBlank(message = "验证码不能为空") String code) { }
    public record LoginResponse(String token, String tokenType, long expiresIn, String username, List<String> authorities) { }
    private final AuthenticationManager manager;
    private final JwtService jwt;
    private final SmsCodeService sms;
    private final SecurityProperties properties;
    public LessonAuthController(@Qualifier("lessonAuthenticationManager") AuthenticationManager manager,
                                JwtService jwt, SmsCodeService sms, SecurityProperties properties) {
        this.manager = manager; this.jwt = jwt; this.sms = sms; this.properties = properties;
    }
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return response(manager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(request.getUsername(), request.getPassword())));
    }
    @PostMapping("/sms/code")
    public Result<String> code(@RequestParam String phone) {
        String code = sms.sendCode(phone);
        return Result.ok(properties.getSms().isExposeCode() ? "验证码已发送（演示模式，验证码：" + code + "）" : "验证码已发送");
    }
    @PostMapping("/sms/login")
    public Result<LoginResponse> sms(@Valid @RequestBody SmsLoginRequest request) {
        return response(manager.authenticate(new SmsCodeAuthenticationToken(request.phone(), request.code())));
    }
    private Result<LoginResponse> response(Authentication authentication) {
        return Result.ok(new LoginResponse(jwt.generate(authentication), "Bearer", jwt.expiresIn(), authentication.getName(),
                authentication.getAuthorities().stream().map(a -> a.getAuthority()).toList()));
    }
}
