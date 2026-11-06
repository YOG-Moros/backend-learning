package top.cch.doc.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "图书信息")
public class Book {

    @Schema(description = "主键", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "书名", example = "深入理解 Java 虚拟机", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "作者", example = "周志明", requiredMode = Schema.RequiredMode.REQUIRED)
    private String author;

    @Schema(description = "13 位 ISBN", example = "9787111641247", requiredMode = Schema.RequiredMode.REQUIRED)
    private String isbn;

    @Schema(description = "分类", example = "TECH", requiredMode = Schema.RequiredMode.REQUIRED)
    private Category category;

    @Schema(description = "单价，单位元，保留两位小数", example = "129.00")
    private BigDecimal price;

    @Schema(description = "库存", example = "10")
    private Integer stock;

    @Schema(description = "出版日期", example = "2019-05-01")
    private LocalDate publishDate;

    @Schema(description = "创建时间，由服务端写入", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createTime;
}
