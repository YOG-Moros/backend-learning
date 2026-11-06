package top.cch.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author cch
 * @date 2026/10/28
 * @description UserCreateRequest 新增账号请求：roles 是数组，一个账号可挂多个角色
 **/
@Data
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度 3-32 位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度 6-64 位")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称不能超过 64 字")
    private String nickname;

    @NotEmpty(message = "角色不能为空")
    private List<String> roles;
}
