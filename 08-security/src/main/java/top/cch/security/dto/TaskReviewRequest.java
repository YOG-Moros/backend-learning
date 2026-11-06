package top.cch.security.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author cch
 * @date 2026/10/28
 * @description TaskReviewRequest 批改任务请求
 **/
@Data
public class TaskReviewRequest {

    @NotNull(message = "分数不能为空")
    @Min(value = 0, message = "分数为 0-100 的整数")
    @Max(value = 100, message = "分数为 0-100 的整数")
    private Integer score;

    @Size(max = 500, message = "评语不能超过 500 字")
    private String comment;
}
