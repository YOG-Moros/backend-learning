package top.cch.mybatis;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import top.cch.mybatis.entity.Student;
import top.cch.mybatis.service.StudentService;
import top.cch.mybatis.vo.ScoreSheetVO;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 学生成绩查询案例：单表查询、分组统计、两种多表查询的对照
 *
 * <p>fixture 自己准备，不依赖 data.sql 的种子数据：
 * 两个在读学生（张伟、李娜），一个已逻辑删除的学生（赵雪），
 * 两门课、四条成绩，其中一条属于赵雪。</p>
 */
@SpringBootTest
class StudentServiceTest {

    @Resource
    private StudentService studentService;
    @Resource
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void seed() {
        jdbcTemplate.execute("DELETE FROM t_score");
        jdbcTemplate.execute("DELETE FROM t_student");
        jdbcTemplate.execute("DELETE FROM t_course");

        jdbcTemplate.update("""
                INSERT INTO t_student (id, name, gender, age, class_name, create_time, update_time, deleted)
                VALUES (1, '张伟', 1, 20, '软件2411', '2026-09-01 08:00:00', '2026-09-01 08:00:00', 0),
                       (2, '李娜', 2, 19, '软件2411', '2026-09-01 08:00:00', '2026-09-01 08:00:00', 0),
                       (6, '赵雪', 2, 19, '软件2421', '2026-09-01 08:00:00', '2026-09-01 08:00:00', 1)
                """);
        jdbcTemplate.update("""
                INSERT INTO t_course (id, name, teacher, credit)
                VALUES (1, '后端工程化开发', '许老师', 4),
                       (2, '前端工程化开发', '许老师', 3)
                """);
        jdbcTemplate.update("""
                INSERT INTO t_score (id, student_id, course_id, score, create_time, update_time, deleted)
                VALUES (1, 1, 1, 88.50, '2026-10-10 09:00:00', '2026-10-10 09:00:00', 0),
                       (2, 1, 2, 92.00, '2026-10-10 09:00:00', '2026-10-10 09:00:00', 0),
                       (3, 2, 1, 95.00, '2026-10-10 09:00:00', '2026-10-10 09:00:00', 0),
                       (4, 6, 1, 90.00, '2026-10-10 09:00:00', '2026-10-10 09:00:00', 0)
                """);
    }

    /* ==================== 单表查询 ==================== */

    @Test
    void getStudentReturnsNullForDeletedAndMissing() {
        assertEquals("张伟", studentService.getStudent(1L).getName());
        // 赵雪 deleted=1，getById 自动拼 deleted=0，查不到返回 null 而不是抛异常
        assertNull(studentService.getStudent(6L), "逻辑删除的学生应查不到");
        assertNull(studentService.getStudent(999L), "不存在的 id 返回 null");
    }

    @Test
    void listAllExcludesDeleted() {
        List<Student> all = studentService.listAll();
        assertEquals(2, all.size(), "三个学生只剩两个，赵雪被 @TableLogic 过滤");
        assertThat(all).extracting(Student::getName).containsExactly("张伟", "李娜");
    }

    @Test
    void listByClassFiltersAndOrders() {
        List<Student> list = studentService.listByClass("软件2411");
        assertThat(list).extracting(Student::getName).containsExactly("张伟", "李娜");
        assertTrue(studentService.listByClass("软件9999").isEmpty());
    }

    @Test
    void searchByNameSupportsLikeAndEmpty() {
        assertThat(studentService.searchByName("张"))
                .extracting(Student::getName)
                .containsExactly("张伟");
        // 不传姓名时 like 条件整段跳过，等于查全部
        assertEquals(2, studentService.searchByName(null).size());
        assertEquals(2, studentService.searchByName("").size());
    }

    @Test
    void listByAgeRangeHandlesNullBounds() {
        List<Student> list = studentService.listByAgeRange(19, 20);
        // 先按年龄升序（李娜 19 在前），年龄相同再按 id
        assertThat(list).extracting(Student::getName).containsExactly("李娜", "张伟");
        // 两端不传等于查全部
        assertEquals(2, studentService.listByAgeRange(null, null).size());
        assertTrue(studentService.listByAgeRange(30, null).isEmpty());
    }

    @Test
    void pageStudentsReturnsTotalAndPages() {
        Page<Student> page = studentService.pageStudents(1, 1);
        assertEquals(2, page.getTotal());
        assertEquals(2, page.getPages());
        assertEquals(1, page.getRecords().size());
        assertEquals("张伟", page.getRecords().get(0).getName());
    }

    @Test
    void listNamesOnlySelectsNameColumn() {
        assertThat(studentService.listNames()).containsExactly("张伟", "李娜");
    }

    /* ==================== 分组统计 ==================== */

    @Test
    void scoreStatGroupsByStudent() {
        List<Map<String, Object>> rows = studentService.scoreStat();

        assertEquals(3, rows.size(), "三个 student_id 各一组");
        Map<String, Object> first = rows.get(0);
        assertEquals(1L, ((Number) first.get("student_id")).longValue(), "总分最高的张伟排第一");
        assertEquals(2L, ((Number) first.get("course_count")).longValue());
        assertEquals(180.50, ((Number) first.get("total_score")).doubleValue(), 1e-9);
        assertEquals(90.25, ((Number) first.get("avg_score")).doubleValue(), 1e-9);

        // 只查成绩表，已逻辑删除的赵雪（student_id=6）依然被统计进来，这正是教程指出的局限
        assertThat(rows)
                .extracting(r -> ((Number) r.get("student_id")).longValue())
                .contains(6L);
    }

    /* ==================== 多表查询：两种写法结果一致 ==================== */

    @Test
    void scoreSheetStepsAndJoinReturnSameResult() {
        List<ScoreSheetVO> steps = studentService.scoreSheetBySteps(1L);
        List<ScoreSheetVO> join = studentService.scoreSheetByJoin(1L);

        assertEquals(steps.size(), join.size());
        for (int i = 0; i < steps.size(); i++) {
            ScoreSheetVO a = steps.get(i);
            ScoreSheetVO b = join.get(i);
            assertEquals(a.getScoreId(), b.getScoreId());
            assertEquals(a.getStudentName(), b.getStudentName());
            assertEquals(a.getCourseName(), b.getCourseName());
            // BigDecimal 精度不同 equals 会判不等，比较数值要用 isEqualByComparingTo
            assertThat(a.getScore()).isEqualByComparingTo(b.getScore());
        }

        assertThat(steps).hasSize(2);
        assertThat(steps.get(0).getStudentName()).isEqualTo("张伟");
        assertThat(steps.get(0).getCourseName()).isEqualTo("后端工程化开发");
        assertThat(steps.get(0).getScore()).isEqualByComparingTo("88.50");
    }

    @Test
    void scoreSheetExcludesDeletedStudentInBothModes() {
        // 分步查：getById 查不到赵雪，直接返回空列表
        assertTrue(studentService.scoreSheetBySteps(6L).isEmpty());
        // join：SQL 里手写了 st.deleted = 0，已删学生同样查不出来
        assertTrue(studentService.scoreSheetByJoin(6L).isEmpty());
        assertTrue(studentService.scoreSheetBySteps(999L).isEmpty());
        assertTrue(studentService.scoreSheetByJoin(999L).isEmpty());
    }
}
