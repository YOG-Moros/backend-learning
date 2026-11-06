package top.cch.security.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author cch
 * @date 2026/10/28
 * @description LoginVO 登录结果：令牌 + 类型 + 过期时间 + 基本信息
 **/
@Data
@AllArgsConstructor
public class LoginVO {

    private String token;

    /** 令牌类型，固定 Bearer，前端拼 Authorization 头用 */
    private String tokenType;

    /** 过期时间，过期后按 401 处理 */
    private LocalDateTime expiresAt;

    private String username;

    private String nickname;

    private List<String> roles;
}
