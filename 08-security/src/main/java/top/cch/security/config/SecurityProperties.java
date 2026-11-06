package top.cch.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.List;

@Data
@Component
@ConfigurationProperties("mqxu.security")
public class SecurityProperties {
    private final Sms sms = new Sms();
    private final Cors cors = new Cors();
    @Data
    public static class Sms {
        private String store = "memory";
        private boolean exposeCode = true;
        private boolean autoRegister = true;
        private Duration codeTtl = Duration.ofMinutes(5);
        private Duration sendInterval = Duration.ofSeconds(60);
        private int maxFailCount = 5;
    }
    @Data
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173");
    }
}
