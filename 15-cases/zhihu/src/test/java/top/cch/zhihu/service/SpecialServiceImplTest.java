package top.cch.zhihu.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.cch.zhihu.common.PageResult;
import top.cch.zhihu.entity.Special;
import top.cch.zhihu.mapper.SpecialMapper;
import top.cch.zhihu.service.impl.SpecialServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SpecialServiceImpl 单元测试
 *
 * <p>Mapper 用 Mockito 打桩，不连数据库，只验证业务层自己的逻辑：
 * 参数兜底、分页上下页判断。</p>
 *
 * @author mqxu
 */
@ExtendWith(MockitoExtension.class)
class SpecialServiceImplTest {

    @Mock
    private SpecialMapper specialMapper;

    @InjectMocks
    private SpecialServiceImpl specialService;

    private static Special special(String id, int followers) {
        return special(id, followers, "热门");
    }

    private static Special special(String id, int followers, String category) {
        Special s = new Special();
        s.setId(id);
        s.setTitle("专题" + id);
        s.setFollowersCount(followers);
        s.setCategory(category);
        return s;
    }

    @Test
    @DisplayName("page：首页 hasPrev=false，还有数据时 hasNext=true")
    void pageFirstPage() {
        when(specialMapper.selectPageList(3, 0)).thenReturn(List.of(special("1", 100), special("2", 90)));
        when(specialMapper.countAll()).thenReturn(97L);

        PageResult<Special> result = specialService.page(3, 0);

        assertEquals(2, result.getList().size());
        assertEquals(97L, result.getTotal());
        assertEquals(3, result.getLimit());
        assertEquals(0, result.getOffset());
        assertFalse(result.isHasPrev());
        assertTrue(result.isHasNext());
    }

    @Test
    @DisplayName("page：最后一页 hasNext=false")
    void pageLastPage() {
        when(specialMapper.selectPageList(3, 96)).thenReturn(List.of(special("97", 1)));
        when(specialMapper.countAll()).thenReturn(97L);

        PageResult<Special> result = specialService.page(3, 96);

        assertEquals(1, result.getList().size());
        assertTrue(result.isHasPrev());
        assertFalse(result.isHasNext());
    }

    @Test
    @DisplayName("page：limit 越界被夹紧到 [1,100]，offset 负数归零")
    void pageClampsArguments() {
        when(specialMapper.selectPageList(anyInt(), anyInt())).thenReturn(List.of());
        when(specialMapper.countAll()).thenReturn(0L);

        specialService.page(9999, -5);

        ArgumentCaptor<Integer> limit = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> offset = ArgumentCaptor.forClass(Integer.class);
        verify(specialMapper).selectPageList(limit.capture(), offset.capture());
        assertEquals(100, limit.getValue());
        assertEquals(0, offset.getValue());
    }

    @Test
    @DisplayName("listByCategory：透传给 Mapper 的分类查询")
    void listByCategory() {
        when(specialMapper.selectByCategory("小众")).thenReturn(List.of(special("9", 3, "小众")));

        List<Special> list = specialService.listByCategory("小众");

        assertEquals(1, list.size());
        assertEquals("小众", list.getFirst().getCategory());
        verify(specialMapper).selectByCategory("小众");
    }

    @Test
    @DisplayName("listCategories：返回数据库中的全部分类")
    void listCategories() {
        when(specialMapper.selectCategories()).thenReturn(List.of("热门", "关注中", "小众"));

        assertEquals(List.of("热门", "关注中", "小众"), specialService.listCategories());
    }
}
