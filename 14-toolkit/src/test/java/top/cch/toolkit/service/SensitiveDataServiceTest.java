package top.cch.toolkit.service;

import cn.hutool.crypto.SecureUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.cch.toolkit.config.ToolkitProperties;
import top.cch.toolkit.dto.CreateCustomerRequest;
import top.cch.toolkit.dto.CustomerView;

import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SensitiveDataServiceTest {

    private static final String AES_KEY = "0123456789abcdef";

    private ToolkitProperties properties;
    private SensitiveDataService service;

    @BeforeEach
    void setUp() {
        properties = new ToolkitProperties();
        properties.getCrypto().setAesKey(AES_KEY);
        service = new SensitiveDataService(properties);
        service.init();
    }

    private CustomerView createCustomer() {
        return service.create(new CreateCustomerRequest(
                "张三", "13800001111", "320102199003071239", "abc123", null));
    }

    @Test
    void 手机号以AES密文入库() {
        CustomerView view = createCustomer();
        Map<String, Object> raw = service.raw(view.id());
        String phoneEncrypted = (String) raw.get("phoneEncrypted");
        assertThat(phoneEncrypted).isNotBlank();
        assertThat(phoneEncrypted).doesNotContain("13800001111");
        assertThat(Base64.getDecoder().decode(phoneEncrypted)).hasSize(16);
        assertThat(SecureUtil.aes(AES_KEY.getBytes()).decryptStr(phoneEncrypted))
                .isEqualTo("13800001111");
    }

    @Test
    void 身份证以AES密文入库() {
        CustomerView view = createCustomer();
        Map<String, Object> raw = service.raw(view.id());
        String idCardEncrypted = (String) raw.get("idCardEncrypted");
        assertThat(idCardEncrypted).isNotBlank();
        assertThat(idCardEncrypted).doesNotContain("320102199003071239");
        assertThat(Base64.getDecoder().decode(idCardEncrypted)).hasSize(32);
        assertThat(SecureUtil.aes(AES_KEY.getBytes()).decryptStr(idCardEncrypted))
                .isEqualTo("320102199003071239");
    }

    @Test
    void 密码使用BCrypt哈希存储() {
        CustomerView view = createCustomer();
        Map<String, Object> raw = service.raw(view.id());
        String passwordHash = (String) raw.get("passwordHash");
        assertThat(passwordHash).startsWith("$2a$");
        assertThat(passwordHash).doesNotContain("abc123");
    }

    @Test
    void admin角色看到明文() {
        CustomerView view = createCustomer();
        CustomerView admin = service.get(view.id(), "admin");
        assertThat(admin.phone()).isEqualTo("13800001111");
        assertThat(admin.idCard()).isEqualTo("320102199003071239");
        assertThat(admin.name()).isEqualTo("张三");
        assertThat(admin.age()).isGreaterThan(18);
    }

    @Test
    void 客服角色看到脱敏数据() {
        CustomerView view = createCustomer();
        CustomerView agent = service.get(view.id(), "service");
        assertThat(agent.phone()).isEqualTo("138****1111");
        assertThat(agent.idCard()).isEqualTo("320102********1239");
    }

    @Test
    void 校验密码正确时返回true() {
        CustomerView view = createCustomer();
        assertThat(service.verifyPassword(view.id(), "abc123")).isTrue();
    }

    @Test
    void 校验密码错误时返回false() {
        CustomerView view = createCustomer();
        assertThat(service.verifyPassword(view.id(), "wrong-password")).isFalse();
    }

    @Test
    void AES密钥不是16字节时启动抛异常() {
        properties.getCrypto().setAesKey("not-16-bytes");
        SensitiveDataService bad = new SensitiveDataService(properties);
        assertThatThrownBy(bad::init).isInstanceOf(IllegalStateException.class);
    }
}
