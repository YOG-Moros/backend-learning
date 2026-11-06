package top.cch.mybatis.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 成绩单 VO：不对应任何表，只描述“返回结果长什么样”
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreSheetVO {

    private Long scoreId;

    private String studentName;

    private String courseName;

    private BigDecimal score;
}
