package top.cch.security.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author cch
 * @date 2026/10/28
 * @description SecUser 用户表（密码只存 BCrypt 密文）
 **/
@Data
@TableName("sec_user")
public class SecUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 3-32 位唯一 */
    private String username;

    /** BCrypt 密文 */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 是否启用：0 停用 / 1 启用 */
    private Boolean enabled;
}
