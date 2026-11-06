package top.cch.actuator.info;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.Info;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("应用信息")
class AppInfoContributorTest {

    @Test
    void startedAtMatchesJvmStartRatherThanRequestTime() {
        var contributor = new AppInfoContributor(new MockEnvironment());
        var builder = new Info.Builder();
        contributor.contribute(builder);
        Map<?, ?> module = (Map<?, ?>) builder.build().getDetails().get("module");
        assertThat(module.get("startedAt")).isEqualTo(java.time.Instant.ofEpochMilli(
                java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime()).toString());
    }

    @Test
    @DisplayName("塞进去的模块信息与负责人信息都在")
    @SuppressWarnings("unchecked")
    void contributesModuleAndOwner() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.application.name", "13-应用监控")
                .withProperty("server.port", "8013");
        AppInfoContributor contributor = new AppInfoContributor(environment);

        Info.Builder builder = new Info.Builder();
        contributor.contribute(builder);
        Info info = builder.build();

        assertThat(info.getDetails()).containsKeys("module", "owner");

        Map<String, Object> module = (Map<String, Object>) info.getDetails().get("module");
        assertThat(module)
                .containsEntry("name", "13-应用监控")
                .containsEntry("port", "8013")
                .containsKey("startedAt");

        Map<String, Object> owner = (Map<String, Object>) info.getDetails().get("owner");
        assertThat(owner).containsEntry("team", "backend-learning");
    }
}
