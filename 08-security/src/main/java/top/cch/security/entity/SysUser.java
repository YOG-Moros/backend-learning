package top.cch.security.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/** 课程账号单独存储，避免覆盖原有实训 sec_user 数据。 */
@Data
@TableName("lesson_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String phone;
    private String password;
    private String nickname;
    private Boolean enabled;
    private Boolean locked;
}
