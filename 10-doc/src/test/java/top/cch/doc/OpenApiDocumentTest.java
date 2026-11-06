package top.cch.doc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("接口文档契约")
class OpenApiDocumentTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exportImportableDocuments() throws Exception {
        java.nio.file.Path directory = java.nio.file.Path.of("target", "openapi");
        java.nio.file.Files.createDirectories(directory);
        for (String group : java.util.List.of("book", "all")) {
            String endpoint = "all".equals(group) ? "/v3/api-docs" : "/v3/api-docs/book";
            String json = mockMvc.perform(get(endpoint)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.openapi").exists()).andExpect(jsonPath("$.paths").isMap())
                    .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
            String filename = "all".equals(group) ? "all.openapi.json" : "10-doc.openapi.json";
            java.nio.file.Files.writeString(directory.resolve(filename), json, java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    @Test
    @DisplayName("接口说明来自 @Operation，不是方法名")
    void summaryComesFromAnnotation() throws Exception {
        mockMvc.perform(get("/v3/api-docs/book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/books']['get']['summary']").value("分页查询图书"))
                .andExpect(jsonPath("$['paths']['/books']['post']['summary']").value("新增图书"));
    }

    @Test
    @DisplayName("book 分组只收 /books 下的接口")
    void bookGroupIsolated() throws Exception {
        mockMvc.perform(get("/v3/api-docs/book"))
                .andExpect(jsonPath("$.paths").value(hasKey("/books")))
                .andExpect(jsonPath("$.paths").value(not(hasKey("/ops/health"))));
    }

    @Test
    @DisplayName("ops 分组只收 /ops 下的接口")
    void opsGroupIsolated() throws Exception {
        mockMvc.perform(get("/v3/api-docs/ops"))
                .andExpect(jsonPath("$.paths").value(hasKey("/ops/health")))
                .andExpect(jsonPath("$.paths").value(not(hasKey("/books"))));
    }

    @Test
    @DisplayName("@ParameterObject 把查询对象摊平成独立参数")
    void queryObjectFlattened() throws Exception {
        mockMvc.perform(get("/v3/api-docs/book"))
                .andExpect(jsonPath("$['paths']['/books']['get']['parameters'][*].name")
                        .value(hasItem("pageNum")))
                .andExpect(jsonPath("$['paths']['/books']['get']['parameters'][*].name")
                        .value(hasItem("keyword")));
    }

    @Test
    @DisplayName("路径参数带说明、示例值与必填标记")
    void pathParameterDocumented() throws Exception {
        mockMvc.perform(get("/v3/api-docs/book"))
                .andExpect(jsonPath("$['paths']['/books/{id}']['get']['parameters'][0]['name']").value("id"))
                .andExpect(jsonPath("$['paths']['/books/{id}']['get']['parameters'][0]['description']").value("图书 ID"))
                .andExpect(jsonPath("$['paths']['/books/{id}']['get']['parameters'][0]['required']").value(true));
    }

    @Test
    @DisplayName("校验注解进入必填列表")
    void requiredFieldsDocumented() throws Exception {
        mockMvc.perform(get("/v3/api-docs/book"))
                .andExpect(jsonPath("$.components.schemas.BookCreateRequest.required")
                        .value(hasItem("title")))
                .andExpect(jsonPath("$.components.schemas.BookCreateRequest.required")
                        .value(hasItem("isbn")));
    }

    @Test
    @DisplayName("枚举内联在 Book 的 category 字段上")
    void categoryEnumInline() throws Exception {
        mockMvc.perform(get("/v3/api-docs/book"))
                .andExpect(jsonPath("$.components.schemas.Book.properties.category.enum")
                        .value(hasItem("TECH")))
                .andExpect(jsonPath("$.components.schemas.Book.properties.category.enum")
                        .value(hasItem("SCIENCE")))
                .andExpect(jsonPath("$.components.schemas").value(not(hasKey("Category"))));
    }

    @Test
    @DisplayName("文档元信息包含标题、版本、联系人与许可证")
    void infoMetadata() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.info.title").value("图书管理接口"))
                .andExpect(jsonPath("$.info.version").value("1.0.0"))
                .andExpect(jsonPath("$.info.contact.name").value("cch"))
                .andExpect(jsonPath("$.info.contact.email").value("cch@example.com"))
                .andExpect(jsonPath("$.info.license.name").value("MIT"));
    }

    @Test
    @DisplayName("服务器列表包含本地与生产")
    void serversDocumented() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.servers.length()").value(2))
                .andExpect(jsonPath("$['servers'][0]['url']").value("http://127.0.0.1:8011"))
                .andExpect(jsonPath("$['servers'][0]['description']").value("本地开发"))
                .andExpect(jsonPath("$['servers'][1]['url']").value("https://api.example.com"));
    }

    @Test
    @DisplayName("响应码列表与请求体示例写进了文档")
    void responsesAndExampleDocumented() throws Exception {
        mockMvc.perform(get("/v3/api-docs/book"))
                .andExpect(jsonPath("$['paths']['/books']['get']['responses']").value(hasKey("200")))
                .andExpect(jsonPath("$['paths']['/books']['get']['responses']").value(hasKey("400")))
                .andExpect(jsonPath("$['paths']['/books']['post']['responses']").value(hasKey("409")))
                .andExpect(jsonPath("$['paths']['/books/{id}']['get']['responses']").value(hasKey("404")))
                .andExpect(jsonPath("$['paths']['/books/boom']['get']['responses']").value(hasKey("500")))
                .andExpect(jsonPath("$['paths']['/books']['post']['requestBody']['content']['application/json']['examples']")
                        .value(hasKey("标准示例")));
    }
}
