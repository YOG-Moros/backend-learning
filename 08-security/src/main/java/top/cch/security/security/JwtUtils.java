package top.cch.security.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import top.cch.security.model.LoginUser;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * @author cch
 * @date 2026/10/28
 * @description JwtUtils 令牌生成与解析（jjwt 0.13，HS256）
 **/
@Component
public class JwtUtils {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtUtils(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /** 生成令牌：subject 放用户名，claims 放 uid / 昵称 / 角色，有效期 120 分钟 */
    public String generate(LoginUser user) {
        long now = System.currentTimeMillis();
        long expireAt = now + properties.getExpireMinutes() * 60_000L;
        return Jwts.builder()
                .subject(user.getUsername())
                .id(UUID.randomUUID().toString())
                .claim("uid", user.getId())
                .claim("nickname", user.getNickname())
                .claim("roles", user.getRoles())
                .issuedAt(new Date(now))
                .expiration(new Date(expireAt))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /** 解析并校验令牌（签名、过期），不合法抛 JwtException */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
