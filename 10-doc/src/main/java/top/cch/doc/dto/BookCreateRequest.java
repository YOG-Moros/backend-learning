package top.cch.doc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import top.cch.doc.entity.Category;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "新增图书请求")
public class BookCreateRequest {

    @NotBlank(message = "书名不能为空")
    @Size(max = 100, message = "书名不能超过 100 字")
    @Schema(description = "书名", example = "深入理解 Java 虚拟机", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "作者不能为空")
    @Size(max = 50, message = "作者名不能超过 50 字")
    @Schema(description = "作者", example = "周志明", requiredMode = Schema.RequiredMode.REQUIRED)
    private String author;

    @NotBlank(message = "ISBN 不能为空")
    @Pattern(regexp = "^\\d{13}$", message = "ISBN 必须是 13 位数字")
    @Schema(description = "13 位 ISBN", example = "9787111641247", requiredMode = Schema.RequiredMode.REQUIRED)
    private String isbn;

    @NotNull(message = "分类不能为空")
    @Schema(description = "分类", example = "TECH", requiredMode = Schema.RequiredMode.REQUIRED)
    private Category category;

    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.01", message = "单价必须大于 0")
    @Schema(description = "单价，单位元，保留两位小数", example = "129.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @Min(value = 0, message = "库存不能为负数")
    @Schema(description = "库存，可不填，默认 0", example = "15")
    private Integer stock;

    @NotNull(message = "出版日期不能为空")
    @Schema(description = "出版日期", example = "2019-05-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate publishDate;
}
