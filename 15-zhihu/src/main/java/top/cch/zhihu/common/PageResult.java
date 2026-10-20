package top.cch.zhihu.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 分页结果封装
 *
 * <p>total 是总数，前端用它判断「上一页 / 下一页」按钮是否要禁用；
 * hasNext / hasPrev 后端直接算好，前端少写逻辑。</p>
 *
 * @author mqxu
 */
@Data
@AllArgsConstructor
public class PageResult<T> {

    /** 当前页数据 */
    private List<T> list;
    /** 总条数 */
    private long total;
    /** 每页条数 */
    private int limit;
    /** 偏移量（等价于 (页码 - 1) * limit） */
    private int offset;
    /** 是否还有下一页 */
    private boolean hasNext;
    /** 是否还有上一页 */
    private boolean hasPrev;

    public static <T> PageResult<T> of(List<T> list, long total, int limit, int offset) {
        boolean hasNext = offset + list.size() < total;
        boolean hasPrev = offset > 0;
        return new PageResult<>(list, total, limit, offset, hasNext, hasPrev);
    }
}
