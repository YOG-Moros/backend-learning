package top.cch.doc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.cch.doc.entity.Category;

@Data
@Schema(description = "图书查询条件")
public class BookQuery {

    @Schema(description = "页码，从 1 开始", example = "1", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", example = "10", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "关键字，匹配书名或作者", example = "Java")
    private String keyword;

    @Schema(description = "分类", example = "TECH")
    private Category category;
}
