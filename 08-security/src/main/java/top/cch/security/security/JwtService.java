package top.cch.security.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/** 独立 issuer 避免课程令牌与旧任务系统的同名账号混用。 */
@Service
public class JwtService {
    private final SecretKey key;
    private final JwtProperties properties;
    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
    public String generate(Authentication authentication) {
        Instant now = Instant.now();
        return Jwts.builder().issuer("security-lesson").subject(authentication.getName())
                .id(UUID.randomUUID().toString()).issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiresIn())))
                .claim("authorities", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .signWith(key).compact();
    }
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).requireIssuer("security-lesson").build().parseSignedClaims(token).getPayload();
    }
    public long expiresIn() { return properties.getExpireMinutes() * 60; }
}
