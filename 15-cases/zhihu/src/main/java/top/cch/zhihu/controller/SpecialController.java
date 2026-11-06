package top.cch.zhihu.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.cch.zhihu.common.PageResult;
import top.cch.zhihu.common.ResponseResult;
import top.cch.zhihu.entity.Special;
import top.cch.zhihu.service.SpecialService;

import java.util.List;

/**
 * 专题接口
 *
 * <ul>
 *     <li>GET /api/v1/special/all                    全部专题</li>
 *     <li>GET /api/v1/special/category/{category}    按分类查询（练习 2）</li>
 *     <li>GET /api/v1/special/page?limit=&offset=     分页查询（练习 3）</li>
 *     <li>GET /api/v1/special/categories             分类列表（前端下拉框）</li>
 * </ul>
 *
 * @author mqxu
 */
@RestController
@RequestMapping("/api/v1/special")
@RequiredArgsConstructor
public class SpecialController {

    private final SpecialService specialService;

    @GetMapping("/all")
    public ResponseResult<List<Special>> all() {
        return ResponseResult.ok(specialService.listAll());
    }

    @GetMapping("/category/{category}")
    public ResponseResult<List<Special>> byCategory(@PathVariable String category) {
        // Spring Boot 3+ 默认按 UTF-8 解码请求行，中文路径变量直接就是正确字符，无需再手工转码
        return ResponseResult.ok(specialService.listByCategory(category));
    }

    @GetMapping("/page")
    public ResponseResult<PageResult<Special>> page(@RequestParam(defaultValue = "3") int limit,
                                                    @RequestParam(defaultValue = "0") int offset) {
        return ResponseResult.ok(specialService.page(limit, offset));
    }

    @GetMapping("/categories")
    public ResponseResult<List<String>> categories() {
        return ResponseResult.ok(specialService.listCategories());
    }
}
