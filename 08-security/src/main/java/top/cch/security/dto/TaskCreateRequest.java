package top.cch.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author cch
 * @date 2026/10/28
 * @description TaskCreateRequest 提交任务请求
 **/
@Data
public class TaskCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题不能超过 100 字")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容不能超过 2000 字")
    private String content;
}
