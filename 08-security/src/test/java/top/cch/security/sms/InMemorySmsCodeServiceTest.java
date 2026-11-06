package top.cch.security.sms;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.server.ResponseStatusException;
import top.cch.security.config.SecurityProperties;
import java.time.Clock;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class InMemorySmsCodeServiceTest {
    private final SecurityProperties properties = new SecurityProperties();
    private final Clock clock = mock(Clock.class);
    private final String phone = "13900001111";

    @Test void codeIsSixDigitsAndConsumedOnce() {
        var service = new InMemorySmsCodeService(properties, clock);
        String code = service.sendCode(phone);
        assertThat(code).matches("[0-9]{6}");
        service.verify(phone, code);
        assertThatThrownBy(() -> service.verify(phone, code)).isInstanceOf(BadCredentialsException.class);
    }
    @Test void rateLimitAndExpiryUseIndependentDeadlines() {
        var service = new InMemorySmsCodeService(properties, clock);
        String code = service.sendCode(phone);
        assertThatThrownBy(() -> service.sendCode(phone)).isInstanceOf(ResponseStatusException.class);
        when(clock.millis()).thenReturn(300_000L);
        assertThatThrownBy(() -> service.verify(phone, code)).isInstanceOf(BadCredentialsException.class);
        assertThat(service.sendCode(phone)).matches("[0-9]{6}");
    }
    @Test void wrongAttemptsInvalidateEvenCorrectAnswer() {
        properties.getSms().setMaxFailCount(2);
        var service = new InMemorySmsCodeService(properties, clock);
        String code = service.sendCode(phone);
        assertThatThrownBy(() -> service.verify(phone, "wrong")).isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> service.verify(phone, "wrong")).isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> service.verify(phone, code)).isInstanceOf(BadCredentialsException.class);
    }
    @Test void onlyOneConcurrentVerificationSucceeds() throws Exception {
        var service = new InMemorySmsCodeService(properties, clock);
        String code = service.sendCode(phone);
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(8)) {
            var futures = new java.util.ArrayList<Future<Boolean>>();
            for (int i=0;i<8;i++) futures.add(pool.submit(() -> {
                start.await();
                try { service.verify(phone, code); return true; } catch (BadCredentialsException e) { return false; }
            }));
            start.countDown();
            int successes=0;
            for (var future : futures) if (future.get(5, TimeUnit.SECONDS)) successes++;
            assertThat(successes).isEqualTo(1);
        }
    }
}
