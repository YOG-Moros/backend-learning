package top.cch.security.sms;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import top.cch.security.config.SecurityProperties;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mqxu.security.sms", name = "store", havingValue = "redis")
public class RedisSmsCodeService implements SmsCodeService {
    private final StringRedisTemplate redis;
    private final SecurityProperties properties;
    private final SecureRandom random = new SecureRandom();
    private static final DefaultRedisScript<Long> SEND = new DefaultRedisScript<>("""
            if redis.call('exists',KEYS[2]) == 1 then return 0 end
            redis.call('set',KEYS[2],'1','PX',ARGV[2])
            redis.call('hset',KEYS[1],'code',ARGV[1],'failures',0,'used',0)
            redis.call('pexpire',KEYS[1],ARGV[3]); return 1
            """, Long.class);
    private static final DefaultRedisScript<Long> VERIFY = new DefaultRedisScript<>("""
            if redis.call('exists',KEYS[1]) == 0 or redis.call('hget',KEYS[1],'used') == '1' then return -1 end
            if redis.call('hget',KEYS[1],'code') ~= ARGV[1] then
              local n=redis.call('hincrby',KEYS[1],'failures',1)
              if n >= tonumber(ARGV[2]) then redis.call('hset',KEYS[1],'used',1); return -2 end
              return 0
            end
            redis.call('hset',KEYS[1],'used',1); return 1
            """, Long.class);

    @Override
    public String sendCode(String phone) {
        InMemorySmsCodeService.validatePhone(phone);
        String code = String.format(java.util.Locale.ROOT, "%06d", random.nextInt(1_000_000));
        Long result = redis.execute(SEND, List.of("sms:lesson:" + phone, "sms:lesson:limit:" + phone), code,
                String.valueOf(properties.getSms().getSendInterval().toMillis()), String.valueOf(properties.getSms().getCodeTtl().toMillis()));
        if (!Long.valueOf(1).equals(result)) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "请稍后再发送验证码");
        return code;
    }

    @Override
    public void verify(String phone, String code) {
        InMemorySmsCodeService.validatePhone(phone);
        Long result = redis.execute(VERIFY, List.of("sms:lesson:" + phone), code == null ? "" : code,
                String.valueOf(properties.getSms().getMaxFailCount()));
        if (!Long.valueOf(1).equals(result)) throw new BadCredentialsException("验证码错误、已使用或已过期");
    }
}
