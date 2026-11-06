package top.cch.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import top.cch.security.security.JwtProperties;
import top.cch.security.security.JwtService;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {
    private JwtService service(String secret, long minutes) {
        var properties=new JwtProperties();
        properties.setSecret(secret); properties.setExpireMinutes(minutes);
        return new JwtService(properties);
    }
    @Test void expiredTokensAndDifferentSigningKeysAreRejected() {
        String key="test-only-key-at-least-thirty-two-bytes-long";
        var authentication=UsernamePasswordAuthenticationToken.authenticated("alice",null,List.of());
        String expired=service(key,-1).generate(authentication);
        assertThatThrownBy(() -> service(key,120).parse(expired)).isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
        String token=service(key,120).generate(authentication);
        assertThatThrownBy(() -> service("another-test-key-at-least-thirty-two-bytes",120).parse(token))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }
}
