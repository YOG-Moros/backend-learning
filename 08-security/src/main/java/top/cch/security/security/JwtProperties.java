package top.cch.security.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author cch
 * @date 2026/10/28
 * @description JwtProperties JWT 配置（application.yml 中 jwt.* ）
 **/
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** HS256 密钥，至少 32 字节 */
    private String secret;

    /** 令牌有效期（分钟），不刷新 */
    private long expireMinutes = 120;
}
