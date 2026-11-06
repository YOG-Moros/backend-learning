package top.cch.deploy.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/deploy")
public class DeployController {

    private final Environment environment;

    private final BuildProperties buildProperties;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    private final LocalDateTime startedAt = LocalDateTime.now();

    public DeployController(Environment environment,
                            @Autowired(required = false) BuildProperties buildProperties) {
        this.environment = environment;
        this.buildProperties = buildProperties;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> data = new LinkedHashMap<>();

        Map<String, Object> build = new LinkedHashMap<>();
        if (buildProperties != null) {
            build.put("artifact", buildProperties.getArtifact());
            build.put("version", buildProperties.getVersion());
            build.put("time", String.valueOf(buildProperties.getTime()));
        } else {
            build.put("note", "未生成 build-info，执行 mvn package 后才有");
        }
        data.put("build", build);

        Map<String, Object> runtime = new LinkedHashMap<>();
        runtime.put("application", environment.getProperty("spring.application.name"));
        runtime.put("profiles", activeProfile);
        runtime.put("port", environment.getProperty("server.port"));
        runtime.put("javaVersion", System.getProperty("java.version"));
        runtime.put("os", System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        runtime.put("hostname", hostname());
        runtime.put("pid", ManagementFactory.getRuntimeMXBean().getPid());
        runtime.put("startedAt", startedAt.toString());
        runtime.put("uptime", humanUptime());
        data.put("runtime", runtime);

        return data;
    }

    @GetMapping("/pipeline")
    public Map<String, Object> pipeline() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("构建", "mvn -pl 14-deploy -am package -DskipTests");
        data.put("镜像", "docker build -f 14-deploy/Dockerfile -t backend-learning/14-deploy:1.0.0 .");
        data.put("运行", "docker run -p 8014:8014 -e SPRING_PROFILES_ACTIVE=prod backend-learning/14-deploy:1.0.0");
        data.put("编排", "docker compose -f 14-deploy/docker-compose.yml up -d");
        data.put("探活", "GET /actuator/health");
        return data;
    }

    @GetMapping("/hello")
    public String hello() {
        return "部署与 CI/CD 模块";
    }

    private String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.warn("获取主机名失败：{}", e.getMessage());
            return "unknown";
        }
    }

    private String humanUptime() {
        Duration d = Duration.between(startedAt, LocalDateTime.now());
        return "%d 分 %d 秒".formatted(d.toMinutes(), d.toSecondsPart());
    }
}
