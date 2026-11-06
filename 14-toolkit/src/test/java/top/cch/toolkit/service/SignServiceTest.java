package top.cch.toolkit.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.cch.toolkit.common.BusinessException;
import top.cch.toolkit.common.ErrorCode;
import top.cch.toolkit.config.ToolkitProperties;
import top.cch.toolkit.dto.SignResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignServiceTest {

    private ToolkitProperties properties;
    private SignService service;

    @BeforeEach
    void setUp() {
        properties = new ToolkitProperties();
        properties.getSign().setAppSecret("unit-test-secret");
        service = new SignService(properties);
        service.init();
    }

    @AfterEach
    void tearDown() {
        service.destroy();
    }

    @Test
    void 规范串是四行拼接格式() {
        SignResponse demo = service.demo("toolkit-demo", 1700000000L, "nonce-abc", "{\"orderId\":1}");
        assertThat(demo.canonical())
                .isEqualTo("toolkit-demo\n1700000000\nnonce-abc\n{\"orderId\":1}");
        assertThat(demo.canonical().split("\n")).hasSize(4);
    }

    @Test
    void 有效签名校验通过() {
        SignResponse demo = service.demo(null, null, "nonce-ok", "{\"amount\":100}");
        assertThatCode(() -> service.verify(demo.appId(), demo.timestamp(), demo.nonce(),
                demo.sign(), demo.body())).doesNotThrowAnyException();
    }

    @Test
    void 重复提交同一nonce抛重放错误() {
        SignResponse demo = service.demo(null, null, "nonce-replay", "{\"amount\":100}");
        service.verify(demo.appId(), demo.timestamp(), demo.nonce(), demo.sign(), demo.body());
        assertThatThrownBy(() -> service.verify(demo.appId(), demo.timestamp(), demo.nonce(),
                demo.sign(), demo.body()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SIGN_REPLAY);
    }

    @Test
    void 篡改body抛签名不正确() {
        SignResponse demo = service.demo(null, null, "nonce-tamper", "{\"amount\":100}");
        assertThatThrownBy(() -> service.verify(demo.appId(), demo.timestamp(), demo.nonce(),
                demo.sign(), "{\"amount\":999}"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SIGN_INVALID);
    }

    @Test
    void 时间戳超出窗口抛请求已过期() {
        long now = System.currentTimeMillis() / 1000;
        SignResponse stale = service.demo("toolkit-demo", now - 301, "nonce-past", "{}");
        assertThatThrownBy(() -> service.verify(stale.appId(), stale.timestamp(), stale.nonce(),
                stale.sign(), "{}"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SIGN_EXPIRED);

        SignResponse future = service.demo("toolkit-demo", now + 301, "nonce-future", "{}");
        assertThatThrownBy(() -> service.verify(future.appId(), future.timestamp(), future.nonce(),
                future.sign(), "{}"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SIGN_EXPIRED);
    }

    @Test
    void 时间戳正好差三百秒时通过() throws InterruptedException {
        long now;
        do {
            Thread.sleep(50);
            now = System.currentTimeMillis() / 1000;
        } while (System.currentTimeMillis() % 1000 > 300);
        long edge = now - 300;
        SignResponse demo = service.demo("toolkit-demo", edge, "nonce-edge", "{}");
        assertThatCode(() -> service.verify("toolkit-demo", String.valueOf(edge), "nonce-edge",
                demo.sign(), "{}")).doesNotThrowAnyException();
    }

    @Test
    void 未知appId与缺失参数被拒绝() {
        SignResponse demo = service.demo("other-app", null, "nonce-other", "{}");
        assertThatThrownBy(() -> service.verify(demo.appId(), demo.timestamp(), demo.nonce(),
                demo.sign(), "{}"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SIGN_INVALID);

        assertThatThrownBy(() -> service.verify(null, demo.timestamp(), demo.nonce(),
                demo.sign(), "{}"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SIGN_MISSING);
        assertThatThrownBy(() -> service.verify("toolkit-demo", demo.timestamp(), demo.nonce(),
                null, "{}"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SIGN_MISSING);
    }
}
