package top.cch.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    private String email;

    @jakarta.validation.constraints.Min(value = 0, message = "年龄不能小于0")
    @jakarta.validation.constraints.Max(value = 150, message = "年龄不能大于150")
    private Integer age;
}
