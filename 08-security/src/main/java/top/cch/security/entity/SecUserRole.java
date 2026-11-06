package top.cch.security.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author cch
 * @date 2026/10/28
 * @description SecUserRole 用户-角色关联表（多对多）
 **/
@Data
@TableName("sec_user_role")
public class SecUserRole {

    private Long userId;

    private Long roleId;
}
