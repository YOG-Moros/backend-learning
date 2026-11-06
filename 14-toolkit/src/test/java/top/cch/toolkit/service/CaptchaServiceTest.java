package top.cch.toolkit.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.cch.toolkit.common.BusinessException;
import top.cch.toolkit.common.ErrorCode;
import top.cch.toolkit.config.ToolkitProperties;
import top.cch.toolkit.dto.CaptchaResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaptchaServiceTest {

    private ToolkitProperties properties;
    private CaptchaService service;

    @BeforeAll
    static void headless() {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void setUp() {
        properties = new ToolkitProperties();
        properties.getCaptcha().setExpireSeconds(60);
        properties.getCaptcha().setDebugCode(true);
        service = new CaptchaService(properties);
        service.init();
    }

    @AfterEach
    void tearDown() {
        service.destroy();
    }

    @Test
    void 生成验证码返回五个字段() {
        CaptchaResponse response = service.create("line");
        assertThat(response.captchaId()).isNotBlank();
        assertThat(response.image()).isNotBlank();
        assertThat(response.expireSeconds()).isEqualTo(60);
        assertThat(response.type()).isEqualTo("line");
        assertThat(response.code()).isNotBlank();
    }

    @Test
    void 图片是Png格式的DataUri() {
        CaptchaResponse response = service.create("line");
        assertThat(response.image()).startsWith("data:image/png;base64,");
    }

    @Test
    void 校验成功一次后立即作废() {
        CaptchaResponse response = service.create("line");
        service.verify(response.captchaId(), response.code());
        assertThatThrownBy(() -> service.verify(response.captchaId(), response.code()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CAPTCHA_NOT_FOUND);
    }

    @Test
    void 连续三次错误后验证码作废() {
        CaptchaResponse response = service.create("line");
        for (int i = 0; i < 2; i++) {
            assertThatThrownBy(() -> service.verify(response.captchaId(), "wrong"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CAPTCHA_WRONG);
        }
        assertThatThrownBy(() -> service.verify(response.captchaId(), "wrong"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CAPTCHA_USED);
        assertThatThrownBy(() -> service.verify(response.captchaId(), response.code()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CAPTCHA_NOT_FOUND);
    }

    @Test
    void 算术验证码返回算好的结果() {
        CaptchaResponse response = service.create("math");
        assertThat(response.type()).isEqualTo("math");
        assertThat(response.code()).matches("\\d+");
        service.verify(response.captchaId(), response.code());
    }

    @Test
    void 未知类型按默认样式生成且可校验() {
        CaptchaResponse response = service.create("no-such-type");
        assertThat(response.captchaId()).isNotBlank();
        assertThat(response.image()).startsWith("data:image/png;base64,");
        assertThat(response.type()).isEqualTo("no-such-type");
        service.verify(response.captchaId(), response.code());
    }

    @Test
    void 过期后校验报验证码不存在() throws InterruptedException {
        properties.getCaptcha().setExpireSeconds(1);
        CaptchaService shortLived = new CaptchaService(properties);
        shortLived.init();
        try {
            CaptchaResponse response = shortLived.create("line");
            Thread.sleep(1200);
            assertThatThrownBy(() -> shortLived.verify(response.captchaId(), response.code()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CAPTCHA_NOT_FOUND);
        } finally {
            shortLived.destroy();
        }
    }
}
