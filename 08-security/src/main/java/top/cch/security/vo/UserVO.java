package top.cch.security.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author cch
 * @date 2026/10/28
 * @description UserVO 用户视图：角色 + 授权字符串，绝不包含 password 字段
 **/
@Data
@AllArgsConstructor
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private Boolean enabled;

    private List<String> roles;

    /** 权限点：由角色摊平而来，前端按钮级控制可用 */
    private List<String> authorities;
}
