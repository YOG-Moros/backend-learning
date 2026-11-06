package top.cch.toolkit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.cch.toolkit.dto.SensitiveResult;
import top.cch.toolkit.dto.TextExtractResult;

import static org.assertj.core.api.Assertions.assertThat;

class TextServiceTest {

    private TextService service;

    @BeforeEach
    void setUp() {
        service = new TextService();
        service.init();
    }

    @Test
    void 提取手机号() {
        TextExtractResult result = service.extract("联系电话 13800005678，备用 19912345678");
        assertThat(result.phones()).containsExactly("13800005678", "19912345678");
    }

    @Test
    void 提取邮箱() {
        TextExtractResult result = service.extract("邮箱 zhangsan@example.com 和 li-si.name@sub.example.org");
        assertThat(result.emails())
                .containsExactly("zhangsan@example.com", "li-si.name@sub.example.org");
    }

    @Test
    void 提取URL() {
        TextExtractResult result = service.extract("文档 https://example.com/docs?a=1&b=2 详见 http://cn.example.org/path");
        assertThat(result.urls())
                .containsExactly("https://example.com/docs?a=1&b=2", "http://cn.example.org/path");
    }

    @Test
    void 提取身份证() {
        TextExtractResult result = service.extract("证件号 320102199003071239，请核对");
        assertThat(result.idCards()).containsExactly("320102199003071239");
    }

    @Test
    void 提取IP() {
        TextExtractResult result = service.extract("来源 192.168.1.100 与 10.0.0.1");
        assertThat(result.ips()).containsExactly("192.168.1.100", "10.0.0.1");
    }

    @Test
    void 身份证里的伪手机号不会被提取() {
        TextExtractResult result = service.extract("身份证 320102199003071239 请核对");
        assertThat(result.idCards()).containsExactly("320102199003071239");
        assertThat(result.phones()).isEmpty();
        assertThat(result.maskedText()).contains("320102********1239");
    }

    @Test
    void 脱敏保留关键位() {
        TextExtractResult result = service.extract(
                "手机 13800005678，邮箱 zhangsan@example.com，身份证 320102199003071239，"
                        + "IP 192.168.1.100，链接 https://example.com/a/b");
        assertThat(result.maskedText()).contains("138****5678");
        assertThat(result.maskedText()).contains("z*******@example.com");
        assertThat(result.maskedText()).contains("320102********1239");
        assertThat(result.maskedText()).contains("192.168.*.*");
        assertThat(result.maskedText()).contains("https://example.com/***");
    }

    @Test
    void 敏感词过滤替换为等长星号并高亮() {
        SensitiveResult result = service.filter("禁止暴力、赌博和诈骗行为，兜售违禁品、代开发票");
        assertThat(result.hits())
                .containsExactlyInAnyOrder("暴力", "赌博", "诈骗", "违禁品", "代开发票");
        assertThat(result.filtered()).isEqualTo("禁止**、**和**行为，兜售***、****");
        assertThat(result.highlighted()).contains("<mark>暴力</mark>", "<mark>赌博</mark>",
                "<mark>诈骗</mark>", "<mark>违禁品</mark>", "<mark>代开发票</mark>");
    }

    @Test
    void 同一敏感词出现多次只计一次() {
        SensitiveResult result = service.filter("赌博输光又赌博，再赌博");
        assertThat(result.hits()).containsExactly("赌博");
        assertThat(result.filtered()).isEqualTo("**输光又**，再**");
    }
}
