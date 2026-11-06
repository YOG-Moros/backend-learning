package top.cch.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "demo")
public class DemoProperties {

    private final Cache cache = new Cache();
    private final Lock lock = new Lock();
    private final Limit limit = new Limit();

    @Data
    public static class Cache {

        private Duration ttl = Duration.ofMinutes(10);

        private Duration nullTtl = Duration.ofMinutes(2);

        private int jitterSeconds = 30;
    }

    @Data
    public static class Lock {

        private int waitSeconds = 3;

        private int leaseSeconds = 10;
    }

    @Data
    public static class Limit {

        private int windowSeconds = 60;

        private int maxRequests = 5;
    }
}
