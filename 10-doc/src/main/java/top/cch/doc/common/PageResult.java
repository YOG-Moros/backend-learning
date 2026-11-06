package top.cch.doc.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "分页结果")
public class PageResult<T> {

    @Schema(description = "当前页数据")
    private List<T> list;

    @Schema(description = "总记录数", example = "5")
    private long total;

    @Schema(description = "当前页码", example = "1")
    private int pageNum;

    @Schema(description = "每页条数", example = "10")
    private int pageSize;
}
