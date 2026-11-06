package top.cch.mybatis.service;

import com.baomidou.mybatisplus.spring.service.IService;
import top.cch.mybatis.entity.Course;

import java.util.List;

/**
 * 课程服务：8.2 练习 3 的 saveBatch / removeByIds，学生成绩查询案例里补了带业务判断的写法
 */
public interface CourseService extends IService<Course> {

    /**
     * 批量新增，底层是 saveBatch
     */
    boolean saveCourses(List<Course> courses);

    /**
     * 按主键批量删除，底层是 removeByIds，t_course 没有逻辑删除列，这里是物理删除
     */
    boolean removeCourses(List<Long> ids);

    /**
     * 查询全部，按 id 升序
     */
    List<Course> listCourses();
}
