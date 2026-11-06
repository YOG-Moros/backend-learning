package top.cch.security.sms;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = {"mqxu.security.sms.store=redis", "spring.data.redis.database=13"})
class RedisSmsCodeServiceTest {
    private static final String PHONE = "13999990001";
    @Autowired SmsCodeService codes;
    @Autowired StringRedisTemplate redis;
    @BeforeEach @AfterEach void cleanup() {
        redis.delete(List.of("sms:lesson:"+PHONE, "sms:lesson:limit:"+PHONE));
    }
    @Test void cooldownAndFailedAttemptsAreEnforced() {
        String code=codes.sendCode(PHONE);
        assertThat(code).matches("\\d{6}");
        assertThatThrownBy(() -> codes.sendCode(PHONE)).isInstanceOf(ResponseStatusException.class);
        for (int i=0;i<5;i++) assertThatThrownBy(() -> codes.verify(PHONE,"wrong")).isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> codes.verify(PHONE,code)).isInstanceOf(BadCredentialsException.class);
        assertThat(redis.getExpire("sms:lesson:"+PHONE)).isPositive();
    }
    @Test void concurrentConsumersCannotReuseCode() throws Exception {
        String code=codes.sendCode(PHONE);
        var start=new CountDownLatch(1);
        try(var pool=Executors.newFixedThreadPool(8)) {
            var results=new java.util.ArrayList<Future<Boolean>>();
            for(int i=0;i<8;i++) results.add(pool.submit(() -> {
                start.await();
                try { codes.verify(PHONE,code); return true; }
                catch(BadCredentialsException expected) { return false; }
            }));
            start.countDown(); int count=0;
            for(var result:results) if(result.get(5,TimeUnit.SECONDS)) count++;
            assertThat(count).isEqualTo(1);
        }
    }
}
