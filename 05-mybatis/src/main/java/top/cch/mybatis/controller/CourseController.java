package top.cch.mybatis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.cch.mybatis.entity.Course;
import top.cch.mybatis.service.CourseService;

import java.util.List;

/**
 * 课程接口：批量新增 / 批量删除 / 查全部
 */
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 批量新增课程
     *
     * <p>body 传课程数组，如 [{"name":"数据结构","teacher":"刘老师","credit":3}]，
     * 空数组直接返回 false，不会进数据库</p>
     */
    @PostMapping("/batch")
    public boolean createBatch(@RequestBody List<Course> courses) {
        return courseService.saveCourses(courses);
    }

    /**
     * 全部课程，按 id 升序
     */
    @GetMapping
    public List<Course> list() {
        return courseService.listCourses();
    }

    /**
     * 按主键批量删除
     *
     * <p>t_course 没有逻辑删除列，这里是物理删除，如 ?ids=4,5</p>
     */
    @DeleteMapping("/batch")
    public boolean deleteBatch(@RequestParam List<Long> ids) {
        return courseService.removeCourses(ids);
    }
}
