package top.cch.security.sms;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import top.cch.security.config.SecurityProperties;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(prefix = "mqxu.security.sms", name = "store", havingValue = "memory", matchIfMissing = true)
public class InMemorySmsCodeService implements SmsCodeService {
    private record Entry(String code, long expiresAt, long nextSendAt, int failures, boolean used) { }
    private final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();
    private final SecurityProperties.Sms properties;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    @org.springframework.beans.factory.annotation.Autowired
    public InMemorySmsCodeService(SecurityProperties properties) { this(properties, Clock.systemUTC()); }
    public InMemorySmsCodeService(SecurityProperties properties, Clock clock) {
        this.properties = properties.getSms(); this.clock = clock;
    }

    @Override
    public synchronized String sendCode(String phone) {
        validatePhone(phone);
        long now = clock.millis();
        entries.entrySet().removeIf(e -> e.getValue().expiresAt <= now && e.getValue().nextSendAt <= now);
        Entry previous = entries.get(phone);
        if (previous != null && now < previous.nextSendAt) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "请稍后再发送验证码");
        String code = String.format(java.util.Locale.ROOT, "%06d", random.nextInt(1_000_000));
        entries.put(phone, new Entry(code, now + properties.getCodeTtl().toMillis(), now + properties.getSendInterval().toMillis(), 0, false));
        return code;
    }

    @Override
    public synchronized void verify(String phone, String code) {
        validatePhone(phone);
        Entry entry = entries.get(phone);
        if (entry == null || clock.millis() >= entry.expiresAt || entry.used) throw new BadCredentialsException("验证码已过期或已使用");
        if (!entry.code.equals(code)) {
            int failures = entry.failures + 1;
            entries.put(phone, new Entry(entry.code, entry.expiresAt, entry.nextSendAt, failures, failures >= properties.getMaxFailCount()));
            throw new BadCredentialsException(failures >= properties.getMaxFailCount() ? "验证码错误次数过多" : "验证码错误");
        }
        entries.put(phone, new Entry(entry.code, entry.expiresAt, entry.nextSendAt, entry.failures, true));
    }

    static void validatePhone(String phone) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "手机号格式不正确");
    }
}
