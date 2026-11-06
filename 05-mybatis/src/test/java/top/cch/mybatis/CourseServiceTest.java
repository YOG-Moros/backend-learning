package top.cch.mybatis;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import top.cch.mybatis.entity.Course;
import top.cch.mybatis.service.CourseService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 8.2 练习 3：saveBatch / removeByIds
 */
@SpringBootTest
class CourseServiceTest {

    @Resource
    private CourseService courseService;
    @Resource
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTable() {
        jdbcTemplate.execute("DELETE FROM t_course");
    }

    private Course course(String name, String teacher, int credit) {
        Course course = new Course();
        course.setName(name);
        course.setTeacher(teacher);
        course.setCredit(credit);
        return course;
    }

    @Test
    void saveBatchInsertsAllAndBackfillsIds() {
        List<Course> courses = List.of(
                course("Java 编程", "张老师", 3),
                course("MySQL 实战", "李老师", 2),
                course("Redis 缓存", "赵老师", 2));

        assertTrue(courseService.saveBatch(courses));
        courses.forEach(c -> assertNotNull(c.getId(), "saveBatch 应回填主键"));
        assertEquals(3, courseService.count());
    }

    @Test
    void removeByIdsDeletesOnlyGivenRows() {
        Course a = course("A", "甲", 1);
        Course b = course("B", "乙", 1);
        Course c = course("C", "丙", 2);
        courseService.saveBatch(List.of(a, b, c));

        assertTrue(courseService.removeByIds(List.of(a.getId(), b.getId())));
        assertEquals(1, courseService.count());
        assertEquals("C", courseService.list().get(0).getName());
    }
}
