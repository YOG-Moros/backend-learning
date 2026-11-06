package top.cch.security.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author cch
 * @date 2026/10/28
 * @description MeVO 当前登录身份：角色 + 授权字符串 + 启用状态，绝不含密码
 **/
@Data
@AllArgsConstructor
public class MeVO {

    private String username;

    private String nickname;

    /** 角色编码：STUDENT / TEACHER / ADMIN */
    private List<String> roles;

    /** 授权字符串：ROLE_ 前缀的角色 + 权限点，与 getAuthorities() 内容一致 */
    private List<String> authorities;

    private Boolean enabled;
}
