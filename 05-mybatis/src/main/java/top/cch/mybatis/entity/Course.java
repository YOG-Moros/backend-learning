package top.cch.mybatis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 课程表：学生成绩查询案例用，8.2 练习 3 的 saveBatch / removeByIds 也复用它
 */
@Data
@TableName("t_course")
public class Course {

    /**
     * BIGINT UNSIGNED —— 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * VARCHAR(64) —— 课程名，建了唯一索引，重名课程插不进来
     */
    private String name;

    /**
     * VARCHAR(32) —— 授课教师，可以为空
     */
    private String teacher;

    /**
     * TINYINT UNSIGNED —— 学分
     */
    private Integer credit;
}
