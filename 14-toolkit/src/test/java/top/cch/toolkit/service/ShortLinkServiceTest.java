package top.cch.toolkit.service;

import cn.hutool.core.util.RandomUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.cch.toolkit.common.BusinessException;
import top.cch.toolkit.common.ErrorCode;
import top.cch.toolkit.config.ToolkitProperties;
import top.cch.toolkit.dto.ShortLinkResponse;
import top.cch.toolkit.dto.ShortLinkStats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

class ShortLinkServiceTest {

    private static final String ALPHABET =
            "23456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ";

    private ToolkitProperties properties;
    private ShortLinkService service;

    @BeforeEach
    void setUp() {
        properties = new ToolkitProperties();
        service = new ShortLinkService(properties);
        service.init();
    }

    @AfterEach
    void tearDown() {
        service.destroy();
    }

    @Test
    void 短码固定为六位() {
        for (int i = 0; i < 20; i++) {
            ShortLinkResponse response = service.create("https://example.com/page" + i, null);
            assertThat(response.code()).hasSize(6);
        }
    }

    @Test
    void 字符集不含易混淆字符() {
        assertThat(ALPHABET).hasSize(54);
        assertThat(ALPHABET).doesNotContain("0", "O", "o", "1", "l", "L", "I", "i");
        for (int i = 0; i < 100; i++) {
            String code = service.create("https://example.com/charset/" + i, null).code();
            for (char c : code.toCharArray()) {
                assertThat(ALPHABET.indexOf(c)).as("短码字符 %s 应在字符集中", c).isNotNegative();
            }
        }
    }

    @Test
    void 短码冲突时自动重试换新码() {
        ShortLinkResponse first = service.create("https://example.com/first", null);
        try (MockedStatic<RandomUtil> mocked = mockStatic(RandomUtil.class)) {
            mocked.when(() -> RandomUtil.randomString(anyString(), anyInt()))
                    .thenReturn(first.code())
                    .thenReturn("zzzzz9");
            ShortLinkResponse second = service.create("https://example.com/second", null);
            assertThat(second.code()).isEqualTo("zzzzz9");
            assertThat(second.url()).isEqualTo("https://example.com/second");
            assertThat(service.resolve(second.code())).isEqualTo("https://example.com/second");
        }
    }

    @Test
    void 解析短码时点击计数自增() {
        ShortLinkResponse response = service.create("https://example.com/target", null);
        for (int i = 0; i < 3; i++) {
            assertThat(service.resolve(response.code())).isEqualTo("https://example.com/target");
        }
        ShortLinkStats stats = service.stats(response.code());
        assertThat(stats.clicks()).isEqualTo(3);
        assertThat(stats.url()).isEqualTo("https://example.com/target");
    }

    @Test
    void 非法URL被拒绝() {
        assertThatThrownBy(() -> service.create("not a url", null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
        assertThatThrownBy(() -> service.create("://no-scheme", null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    void 自定义TTL覆盖默认有效期() {
        long now = System.currentTimeMillis();
        ShortLinkResponse custom = service.create("https://example.com/ttl", 60L);
        assertThat(custom.expireAt()).isBetween(now + 55_000, now + 65_000);

        long defaultSeconds = properties.getShortLink().getExpireSeconds();
        ShortLinkResponse byDefault = service.create("https://example.com/default", null);
        assertThat(byDefault.expireAt())
                .isBetween(now + (defaultSeconds - 5) * 1000, now + (defaultSeconds + 5) * 1000);

        ShortLinkResponse zeroTtl = service.create("https://example.com/zero", 0L);
        assertThat(zeroTtl.expireAt())
                .isBetween(now + (defaultSeconds - 5) * 1000, now + (defaultSeconds + 5) * 1000);
    }

    @Test
    void 过期短码解析时抛异常() throws InterruptedException {
        properties.getShortLink().setExpireSeconds(1);
        ShortLinkService shortLived = new ShortLinkService(properties);
        shortLived.init();
        try {
            ShortLinkResponse response = shortLived.create("https://example.com/short", null);
            Thread.sleep(1200);
            assertThatThrownBy(() -> shortLived.resolve(response.code()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.SHORT_LINK_NOT_FOUND);
        } finally {
            shortLived.destroy();
        }
    }
}
