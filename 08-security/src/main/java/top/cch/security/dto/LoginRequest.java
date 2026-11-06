package top.cch.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author cch
 * @date 2026/10/28
 * @description LoginRequest 登录请求
 **/
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
