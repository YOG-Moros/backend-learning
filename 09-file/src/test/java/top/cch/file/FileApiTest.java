package top.cch.file;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import top.cch.file.common.BusinessException;
import top.cch.file.config.FileProperties;
import top.cch.file.service.FileService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "file.local.base-dir=target/test-uploads",
        "file.max-file-size=1KB"
})
@AutoConfigureMockMvc
class FileApiTest {

    private static final byte[] PNG = java.util.Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8AAAwAB/gEBQ7X7AAAAAElFTkSuQmCC");

    private static final byte[] MZ = {0x4D, 0x5A, (byte) 0x90, 0x00, 0x03, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00};

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileProperties properties;

    @Autowired
    private FileService fileService;

    @BeforeEach
    void clean() throws IOException {
        Path base = Paths.get(properties.getLocal().getBaseDir()).toAbsolutePath().normalize();
        if (!Files.exists(base)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(base)) {
            walk.sorted(Comparator.reverseOrder())
                    .filter(p -> !p.equals(base))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    @Test
    void 上传成功返回符合格式的objectKey() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain",
                "hello".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/file/upload").file(file).param("dir", "docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.objectKey").value(
                        matchesPattern("docs/\\d{4}/\\d{2}/\\d{2}/[0-9a-f]{32}\\.txt")))
                .andExpect(jsonPath("$.data.originalName").value("hello.txt"))
                .andExpect(jsonPath("$.data.storage").value("local"))
                .andExpect(jsonPath("$.data.size").value(5));
    }

    @Test
    void 不传dir时使用默认目录files() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain",
                "hello".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/file/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.objectKey").value(
                        matchesPattern("files/\\d{4}/\\d{2}/\\d{2}/[0-9a-f]{32}\\.txt")));
    }

    @Test
    void 批量上传全部成功() throws Exception {
        MockMultipartFile first = new MockMultipartFile("files", "a.txt", "text/plain", "a".getBytes());
        MockMultipartFile second = new MockMultipartFile("files", "b.txt", "text/plain", "b".getBytes());
        mockMvc.perform(multipart("/file/upload/batch").file(first).file(second).param("dir", "docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].objectKey").value(
                        matchesPattern("docs/\\d{4}/\\d{2}/\\d{2}/[0-9a-f]{32}\\.txt")))
                .andExpect(jsonPath("$.data[1].objectKey").value(
                        matchesPattern("docs/\\d{4}/\\d{2}/\\d{2}/[0-9a-f]{32}\\.txt")));
    }

    @Test
    void 批量中途失败回滚已写入的文件() throws Exception {
        MockMultipartFile good = new MockMultipartFile("files", "a.txt", "text/plain", "a".getBytes());
        MockMultipartFile bad = new MockMultipartFile("files", "b.exe", "application/octet-stream", MZ);
        mockMvc.perform(multipart("/file/upload/batch").file(good).file(bad).param("dir", "docs"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10402));
        Path base = Paths.get(properties.getLocal().getBaseDir()).toAbsolutePath().normalize();
        try (Stream<Path> walk = Files.walk(base)) {
            long remaining = walk.filter(Files::isRegularFile).count();
            assertThat(remaining).isZero();
        }
    }

    @Test
    void 扩展名不在白名单时被拒绝() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "evil.exe", "application/octet-stream", MZ);
        mockMvc.perform(multipart("/file/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10402))
                .andExpect(jsonPath("$.message").value("不支持的扩展名：exe"));
    }

    @Test
    void 可执行文件头被拒绝() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "evil.txt", "text/plain", MZ);
        mockMvc.perform(multipart("/file/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10402))
                .andExpect(jsonPath("$.message").value(containsString("可执行文件")));
    }

    @Test
    void 图片扩展名但文件头不符时被拒绝() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "fake.png", "image/png",
                "这不是PNG只是一段文本".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/file/upload/image").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10402))
                .andExpect(jsonPath("$.message").value(containsString("文件内容与扩展名不符")));
    }

    @Test
    void 超过大小限制返回413() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "big.txt", "text/plain", new byte[2048]);
        mockMvc.perform(multipart("/file/upload").file(file))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.code").value(10403));
    }

    @Test
    void 空文件返回400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        mockMvc.perform(multipart("/file/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10401));
    }

    @Test
    void 非法目录被拒绝() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain",
                "hello".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/file/upload").file(file).param("dir", "../hack"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("目录只能是字母、数字、下划线或连字符")));
    }

    @Test
    void 下载返回attachment与原始内容() throws Exception {
        String key = upload("hello.txt", "hello".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(get("/file/download/" + key))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, "5"))
                .andExpect(content().string("hello"));
    }

    @Test
    void 预览返回inline() throws Exception {
        String key = upload("dot.png", PNG);
        mockMvc.perform(get("/file/preview/" + key))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("inline")));
    }

    @Test
    void 错配图片扩展名与无效签名期限被拒绝() throws Exception {
        mockMvc.perform(multipart("/file/upload/image")
                .file(new MockMultipartFile("file", "fake.jpg", "image/jpeg", PNG)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/file/url/docs/example.txt").param("expireMinutes", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 删除后再下载返回404() throws Exception {
        String key = upload("hello.txt", "hello".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(delete("/file/" + key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(get("/file/download/" + key))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(10404));
    }

    @Test
    void 路径穿越在Web层与Service层都被拦截() throws Exception {
        mockMvc.perform(get("/file/download/%2E%2E/%2E%2E/etc/passwd"))
                .andExpect(status().is4xxClientError());
        assertThatThrownBy(() -> fileService.read("../../etc/passwd"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("非法的文件路径");
    }

    private String upload(String name, byte[] content) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", name, "application/octet-stream", content);
        MvcResult result = mockMvc.perform(multipart("/file/upload").file(file).param("dir", "docs"))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.objectKey");
    }
}
