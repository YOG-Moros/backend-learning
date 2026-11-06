package top.cch.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.cch.mybatis.entity.Score;
import top.cch.mybatis.vo.ScoreSheetVO;

import java.util.List;

@Mapper
public interface ScoreMapper extends BaseMapper<Score> {

    /**
     * 一条 join SQL 直接查出成绩单。
     *
     * <p>三个要点：
     * 别名要对上 VO 的属性名（AS studentName）；逻辑删除要自己写条件（s.deleted / st.deleted），
     * 手写 SQL 不享受 @TableLogic 的自动过滤；ON 里可以顺手带过滤条件。</p>
     */
    @Select("""
            SELECT s.id        AS scoreId,
                   st.name     AS studentName,
                   c.name      AS courseName,
                   s.score     AS score
            FROM t_score s
                     JOIN t_student st ON st.id = s.student_id AND st.deleted = 0
                     JOIN t_course c ON c.id = s.course_id
            WHERE s.deleted = 0
              AND s.student_id = #{studentId}
            ORDER BY s.id
            """)
    List<ScoreSheetVO> selectScoreSheet(@Param("studentId") Long studentId);
}
