package top.cch.security.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author cch
 * @date 2026/10/28
 * @description LoginUser 登录用户信息：JWT 解析后放进 SecurityContext 的主体
 **/
@Data
@AllArgsConstructor
public class LoginUser {

    private Long id;

    private String username;

    private String nickname;

    /** 角色编码：STUDENT / TEACHER / ADMIN */
    private List<String> roles;

    /** 权限点：task:submit / task:review / user:read ... */
    private List<String> permissions;

    /** 账号是否启用（能进上下文的必然是启用账号，给 /auth/me 回显用） */
    private Boolean enabled;
}
