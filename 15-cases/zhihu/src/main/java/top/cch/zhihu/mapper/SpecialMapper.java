package top.cch.zhihu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import top.cch.zhihu.entity.Special;

import java.util.List;

/**
 * 专题 Mapper
 *
 * <p>继承 MyBatis Plus 的 BaseMapper 即可获得 selectList / selectById / insert 等通用方法；
 * 分类查询和分页查询在 SpecialMapper.xml 里手写 SQL。</p>
 *
 * @author mqxu
 */
public interface SpecialMapper extends BaseMapper<Special> {

    /**
     * 按分类查询专题（XML 手写 SQL）
     *
     * @param category 分类名
     * @return 专题列表
     */
    List<Special> selectByCategory(@Param("category") String category);

    /**
     * 分页查询专题（XML 手写 LIMIT / OFFSET）
     *
     * @param limit  每页条数
     * @param offset 偏移量
     * @return 当前页数据
     */
    List<Special> selectPageList(@Param("limit") int limit, @Param("offset") int offset);

    /**
     * 统计总条数（前端分页按钮需要）
     *
     * @return 总条数
     */
    long countAll();

    /**
     * 查询全部已使用的分类，供前端下拉框使用
     *
     * @return 分类名列表
     */
    List<String> selectCategories();
}
