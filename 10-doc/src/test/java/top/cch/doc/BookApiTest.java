package top.cch.doc;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("图书接口行为")
class BookApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("分页查询返回第一页三条数据")
    void pageQuery() throws Exception {
        mockMvc.perform(get("/books")
                        .param("pageNum", "1")
                        .param("pageSize", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(5))
                .andExpect(jsonPath("$.data.list.length()").value(3))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(3));
    }

    @Test
    @DisplayName("关键字与分类一起过滤")
    void filterByKeywordAndCategory() throws Exception {
        mockMvc.perform(get("/books")
                        .param("keyword", "Java")
                        .param("category", "TECH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list[0].title").value("深入理解 Java 虚拟机"));
    }

    @Test
    @DisplayName("按主键查询详情")
    void getById() throws Exception {
        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("深入理解 Java 虚拟机"))
                .andExpect(jsonPath("$.data.category").value("TECH"));
    }

    @Test
    @DisplayName("新增图书返回完整数据")
    void createBook() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"领域驱动设计","author":"Eric Evans","isbn":"9787115104682","category":"TECH","price":89.00,"stock":12,"publishDate":"2020-04-01"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("领域驱动设计"))
                .andExpect(jsonPath("$.data.isbn").value("9787115104682"))
                .andReturn();
        Object id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.id");
        mockMvc.perform(delete("/books/" + id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ISBN 重复返回 409 与业务码 20409")
    void createDuplicateIsbn() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"重复的书","author":"某人","isbn":"9787111641247","category":"TECH","price":10.00,"publishDate":"2020-01-01"}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(20409));
    }

    @Test
    @DisplayName("参数校验失败返回 400 与具体原因")
    void createValidationFailed() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"书名","author":"某人","isbn":"123","category":"TECH","price":10.00,"publishDate":"2020-01-01"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("ISBN 必须是 13 位数字"));
    }

    @Test
    @DisplayName("删除后再查询返回 404 与业务码 20404")
    void deleteThenNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"临时的书","author":"某人","isbn":"9787544291163","category":"OTHER","price":10.00,"publishDate":"2020-01-01"}"""))
                .andExpect(status().isOk())
                .andReturn();
        Object id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.id");
        String idStr = String.valueOf(id);

        mockMvc.perform(delete("/books/" + idStr))
                .andExpect(status().isOk());

        mockMvc.perform(get("/books/" + idStr))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(20404));
    }

    @Test
    @DisplayName("未预期异常返回 500 与统一 JSON")
    void boomEndpoint() throws Exception {
        mockMvc.perform(get("/books/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("系统繁忙，请稍后再试"));
    }
}
