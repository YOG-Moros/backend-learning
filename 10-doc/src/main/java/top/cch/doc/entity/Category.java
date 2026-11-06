package top.cch.doc.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "图书分类")
public enum Category {

    @Schema(description = "计算机与技术") TECH,
    @Schema(description = "文学") LITERATURE,
    @Schema(description = "历史") HISTORY,
    @Schema(description = "科普") SCIENCE,
    @Schema(description = "其他") OTHER
}
