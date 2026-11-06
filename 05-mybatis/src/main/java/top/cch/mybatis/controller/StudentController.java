package top.cch.mybatis.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.cch.mybatis.entity.Student;
import top.cch.mybatis.service.StudentService;
import top.cch.mybatis.vo.ScoreSheetVO;

import java.util.List;
import java.util.Map;

/**
 * 学生成绩接口：单表查询、条件构造、分页、分组统计、多表查询都在这个控制器里
 */
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * 查全部学生
     *
     * <p>库里六条数据只返回五条，id=6 的赵雪 deleted=1，被 @TableLogic 自动过滤</p>
     */
    @GetMapping
    public List<Student> list() {
        return studentService.listAll();
    }

    /**
     * 按班级查学生
     *
     * <p>className 必传，如 /students/class?className=软件2411</p>
     */
    @GetMapping("/class")
    public List<Student> listByClass(@RequestParam String className) {
        return studentService.listByClass(className);
    }

    /**
     * 按姓名模糊查学生
     *
     * <p>name 可不传，不传等于查全部；传“张”能命中“张伟”，like 两边都带百分号</p>
     */
    @GetMapping("/search")
    public List<Student> search(@RequestParam(required = false) String name) {
        return studentService.searchByName(name);
    }

    /**
     * 按年龄区间查学生
     *
     * <p>minAge / maxAge 都可以不传，都不传等于查全部，如 /students/age-range?minAge=19&amp;maxAge=20</p>
     */
    @GetMapping("/age-range")
    public List<Student> listByAgeRange(@RequestParam(required = false) Integer minAge,
                                        @RequestParam(required = false) Integer maxAge) {
        return studentService.listByAgeRange(minAge, maxAge);
    }

    /**
     * 分页查学生
     *
     * <p>current 页码从 1 开始，size 每页条数；返回体里 records 是当前页数据，
     * total 总条数、pages 总页数，如 /students/page?current=1&amp;size=2</p>
     */
    @GetMapping("/page")
    public Page<Student> page(@RequestParam(defaultValue = "1") long current,
                              @RequestParam(defaultValue = "2") long size) {
        return studentService.pageStudents(current, size);
    }

    /**
     * 只取姓名这一列
     *
     * <p>页面只要一个姓名下拉框时用，SQL 只 select name 一列</p>
     */
    @GetMapping("/names")
    public List<String> names() {
        return studentService.listNames();
    }

    /**
     * 每个学生的选课门数、总分、平均分
     *
     * <p>GROUP BY student_id 的分组统计；注意结果里会出现已逻辑删除的赵雪（student_id=6），
     * 因为这条 SQL 只查成绩表，成绩行本身没被删除——这正是后面 join 要解决的问题</p>
     */
    @GetMapping("/statistics")
    public List<Map<String, Object>> statistics() {
        return studentService.scoreStat();
    }

    /**
     * 某个学生的成绩单
     *
     * <p>mode=steps（默认）走分步查询：先查学生，再查成绩，再批量查课程，Java 里拼装；
     * mode=join 走一条 join SQL。两种写法返回结果一模一样，可以对照着看</p>
     */
    @GetMapping("/{id}/scores")
    public List<ScoreSheetVO> scores(@PathVariable Long id,
                                     @RequestParam(defaultValue = "steps") String mode) {
        return "join".equals(mode)
                ? studentService.scoreSheetByJoin(id)
                : studentService.scoreSheetBySteps(id);
    }

    /**
     * 按 id 查一个学生
     *
     * <p>查不到返回 null（不抛异常）；逻辑删除的学生同样返回 null；
     * 放在最后声明，避免把 /class、/page 这些字面路径抢走</p>
     */
    @GetMapping("/{id}")
    public Student get(@PathVariable Long id) {
        return studentService.getStudent(id);
    }
}
