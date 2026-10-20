package top.cch.zhihu.service;

import top.cch.zhihu.common.PageResult;
import top.cch.zhihu.entity.Special;

import java.util.List;

/**
 * 专题业务接口
 *
 * @author mqxu
 */
public interface SpecialService {

    /**
     * 查询全部专题
     *
     * @return 专题列表
     */
    List<Special> listAll();

    /**
     * 按分类查询专题（练习 2）
     *
     * @param category 分类名
     * @return 专题列表
     */
    List<Special> listByCategory(String category);

    /**
     * 分页查询专题（练习 3）
     *
     * @param limit  每页条数
     * @param offset 偏移量
     * @return 分页结果
     */
    PageResult<Special> page(int limit, int offset);

    /**
     * 查询全部分类，供前端下拉框使用
     *
     * @return 分类列表
     */
    List<String> listCategories();
}
