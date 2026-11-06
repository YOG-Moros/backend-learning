package top.cch.security.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("lesson_role")
public class SysRole {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String roleCode;
    private String grantType;

    public String toAuthority() { return "PERM".equals(grantType) ? roleCode : "ROLE_" + roleCode; }
}
