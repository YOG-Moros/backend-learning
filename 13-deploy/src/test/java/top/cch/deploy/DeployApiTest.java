package top.cch.deploy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("部署信息与探针")
class DeployApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("hello 返回模块名")
    void hello() throws Exception {
        mockMvc.perform(get("/deploy/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("部署与 CI/CD 模块"));
    }

    @Test
    @DisplayName("build-info 生效，构建信息不是空壳")
    void infoHasBuildInfo() throws Exception {
        mockMvc.perform(get("/deploy/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.build.artifact").value("13-deploy"))
                .andExpect(jsonPath("$.build.version").value("0.0.1-SNAPSHOT"))
                .andExpect(jsonPath("$.build.time").exists());
    }

    @Test
    @DisplayName("runtime 段带出端口、JDK 版本、主机名与进程号")
    void infoHasRuntimeDetails() throws Exception {
        mockMvc.perform(get("/deploy/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.runtime.application").value("14-部署与CI/CD"))
                .andExpect(jsonPath("$.runtime.port", matchesPattern("\\d+")))
                .andExpect(jsonPath("$.runtime.javaVersion").exists())
                .andExpect(jsonPath("$.runtime.hostname").isNotEmpty())
                .andExpect(jsonPath("$.runtime.pid").isNumber())
                .andExpect(jsonPath("$.runtime.startedAt").exists())
                .andExpect(jsonPath("$.runtime.uptime", matchesPattern("\\d+ 分 \\d+ 秒")));
    }

    @Test
    @DisplayName("pipeline 把五步命令都列出来了")
    void pipelineListsFiveSteps() throws Exception {
        mockMvc.perform(get("/deploy/pipeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.构建").exists())
                .andExpect(jsonPath("$.镜像").exists())
                .andExpect(jsonPath("$.运行").exists())
                .andExpect(jsonPath("$.编排").exists())
                .andExpect(jsonPath("$.探活").exists());
    }

    @Test
    @DisplayName("健康检查整体 UP")
    void healthUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("liveness 与 readiness 两个探针都开了")
    void probesEnabled() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("只暴露 health 与 info，metrics 访问不到")
    void onlyHealthAndInfoExposed() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isNotFound());
    }
}
