package top.cch.actuator.info;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AppInfoContributor implements InfoContributor {

    private final Environment environment;
    private final String startedAt = java.time.Instant.ofEpochMilli(
            java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime()).toString();

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> module = new LinkedHashMap<>();
        module.put("name", environment.getProperty("spring.application.name", "unknown"));
        module.put("profiles", String.join(",", environment.getActiveProfiles()));
        module.put("port", environment.getProperty("server.port"));
        module.put("startedAt", startedAt);

        Map<String, Object> owner = new LinkedHashMap<>();
        owner.put("team", "backend-learning");
        owner.put("contact", "cch@example.com");

        builder.withDetail("module", module).withDetail("owner", owner);
    }
}
