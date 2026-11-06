package top.cch.security.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author cch
 * @date 2026/10/28
 * @description SecRole 角色表
 **/
@Data
@TableName("sec_role")
public class SecRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码：STUDENT / TEACHER / ADMIN */
    private String code;

    /** 角色名称：学生 / 教师 / 管理员 */
    private String name;
}
