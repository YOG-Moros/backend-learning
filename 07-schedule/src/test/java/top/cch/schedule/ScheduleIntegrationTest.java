package top.cch.schedule;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import top.cch.schedule.service.TaskExecutionRecorder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ScheduleIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TaskExecutionRecorder recorder;

    @Test
    @Order(1)
    @DisplayName("等 11 秒：heartBeat 约 4 次、cleanExpiredData 约 2 次、dailyReport 约 1 次，三类任务真实触发")
    void allThreeTriggersFire() throws InterruptedException {
        Thread.sleep(11_000);

        List<TaskExecutionRecorder.Execution> records = recorder.recent(200);
        assertThat(records).extracting(TaskExecutionRecorder.Execution::taskName)
                .contains("heartBeat", "cleanExpiredData", "dailyReport");
        assertThat(countOf("heartBeat")).isBetween(3L, 5L);
        assertThat(countOf("cleanExpiredData")).isBetween(2L, 3L);
        assertThat(countOf("dailyReport")).isBetween(1L, 2L);
    }

    @Test
    @Order(2)
    @DisplayName("注册动态任务成功：返回 success=true，运行中列表包含该任务")
    void registerDynamicTask() {
        ResponseEntity<String> response = postDynamic("dynamicTest", "0/2 * * * * ?");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        JSONObject body = JSONObject.parseObject(response.getBody());
        assertThat(body.getBooleanValue("success")).isTrue();
        assertThat(body.getJSONArray("运行中").toJavaList(String.class)).contains("dynamicTest");
        assertThat(body.getString("cron")).isEqualTo("0/2 * * * * ?");
    }

    @Test
    @Order(3)
    @DisplayName("动态任务注册后真实执行：等待 2.6 秒，执行次数至少为 1")
    void dynamicTaskActuallyFires() throws InterruptedException {
        Thread.sleep(2_600);

        JSONObject body = JSONObject.parseObject(
                restTemplate.getForObject("/schedule/tasks/dynamic", String.class));

        assertThat(body.getJSONArray("运行中").toJavaList(String.class)).contains("dynamicTest");
        assertThat(body.getJSONObject("各任务执行次数").getIntValue("dynamicTest")).isGreaterThanOrEqualTo(1);
    }

    @Test
    @Order(4)
    @DisplayName("重复注册同名任务返回 success=false 而不是 500")
    void duplicateRegisterReturnsFalse() {
        ResponseEntity<String> response = postDynamic("dynamicTest", "0/2 * * * * ?");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        JSONObject body = JSONObject.parseObject(response.getBody());
        assertThat(body.getBooleanValue("success")).isFalse();
        assertThat(body.getString("message")).contains("任务已存在");
    }

    @Test
    @Order(5)
    @DisplayName("非法 cron 表达式返回 success=false 与具体错误信息，而不是 500")
    void invalidCronReturnsFalse() {
        ResponseEntity<String> response = postDynamic("badCron", "这不是 cron");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        JSONObject body = JSONObject.parseObject(response.getBody());
        assertThat(body.getBooleanValue("success")).isFalse();
        assertThat(body.getString("message")).isNotBlank();
    }

    @Test
    @Order(6)
    @DisplayName("取消动态任务：第一次返回已取消，第二次返回任务不存在")
    void cancelDynamicTaskTwice() {
        JSONObject first = JSONObject.parseObject(restTemplate.exchange(
                "/schedule/tasks/dynamic/dynamicTest", HttpMethod.DELETE, null, String.class).getBody());
        assertThat(first.getBooleanValue("success")).isTrue();
        assertThat(first.getString("message")).isEqualTo("已取消");

        JSONObject second = JSONObject.parseObject(restTemplate.exchange(
                "/schedule/tasks/dynamic/dynamicTest", HttpMethod.DELETE, null, String.class).getBody());
        assertThat(second.getBooleanValue("success")).isFalse();
        assertThat(second.getString("message")).isEqualTo("任务不存在");
    }

    @Test
    @Order(7)
    @DisplayName("天气接口返回今日天气数据：fxDate 为今天，六个字段齐全")
    void weatherReturnsToday() {
        ResponseEntity<String> response = restTemplate.getForEntity("/schedule/weather", String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        JSONObject body = JSONObject.parseObject(response.getBody());
        assertThat(body.getBooleanValue("success")).isTrue();
        JSONObject data = body.getJSONObject("data");
        assertThat(data.getString("fxDate")).isEqualTo(LocalDate.now().toString());
        assertThat(data.getString("tempMax")).isNotBlank();
        assertThat(data.getString("tempMin")).isNotBlank();
        assertThat(data.getString("textDay")).isNotBlank();
        assertThat(data.getString("windDirDay")).isNotBlank();
        assertThat(data.getString("windScaleDay")).isNotBlank();
    }

    private long countOf(String taskName) {
        return recorder.stats().stream()
                .filter(item -> taskName.equals(item.get("任务名")))
                .mapToLong(item -> ((Number) item.get("执行次数")).longValue())
                .findFirst()
                .orElse(0L);
    }

    private ResponseEntity<String> postDynamic(String name, String cron) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("name", name);
        form.add("cron", cron);
        return restTemplate.postForEntity("/schedule/tasks/dynamic", new HttpEntity<>(form, headers), String.class);
    }
}
