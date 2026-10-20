package top.cch.zhihu.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.cch.zhihu.common.PageResult;
import top.cch.zhihu.entity.Special;
import top.cch.zhihu.mapper.SpecialMapper;
import top.cch.zhihu.service.SpecialService;

import java.util.List;

/**
 * 专题业务实现
 *
 * @author mqxu
 */
@Service
@RequiredArgsConstructor
public class SpecialServiceImpl implements SpecialService {

    private final SpecialMapper specialMapper;

    @Override
    public List<Special> listAll() {
        // BaseMapper 的通用方法，OrderBy 按关注量倒序
        return specialMapper.selectList(
                Wrappers.<Special>lambdaQuery()
                        .orderByDesc(Special::getFollowersCount)
                        .orderByAsc(Special::getId));
    }

    @Override
    public List<Special> listByCategory(String category) {
        return specialMapper.selectByCategory(category);
    }

    @Override
    public PageResult<Special> page(int limit, int offset) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        int safeOffset = Math.max(0, offset);
        List<Special> list = specialMapper.selectPageList(safeLimit, safeOffset);
        long total = specialMapper.countAll();
        return PageResult.of(list, total, safeLimit, safeOffset);
    }

    @Override
    public List<String> listCategories() {
        return specialMapper.selectCategories();
    }
}
