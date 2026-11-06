package top.cch.redis.dto;

import lombok.Data;

/**
 * 验证码登录入参
 *
 * @author cch
 */
@Data
public class LoginRequest {
    private String phone;
    private String code;
}
