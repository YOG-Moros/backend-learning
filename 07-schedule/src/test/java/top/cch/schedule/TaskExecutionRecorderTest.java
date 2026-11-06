package top.cch.schedule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.cch.schedule.service.TaskExecutionRecorder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TaskExecutionRecorderTest {

    private final TaskExecutionRecorder recorder = new TaskExecutionRecorder();

    @Test
    @DisplayName("recent 倒序返回最新记录，条数不超过 limit")
    void recentReturnsNewestFirst() {
        recorder.record("heartBeat", LocalDateTime.now(), 0, true);
        recorder.record("cleanExpiredData", LocalDateTime.now(), 1, true);
        recorder.record("dailyReport", LocalDateTime.now(), 2, true);

        List<TaskExecutionRecorder.Execution> list = recorder.recent(2);

        assertThat(list).hasSize(2);
        assertThat(list.get(0).taskName()).isEqualTo("dailyReport");
        assertThat(list.get(1).taskName()).isEqualTo("cleanExpiredData");
        assertThat(list.get(0).id()).isGreaterThan(list.get(1).id());
    }

    @Test
    @DisplayName("stats 按任务名分组统计，中文键齐全且按任务名排序")
    void statsGroupedAndSortedByTaskName() {
        recorder.record("heartBeat", LocalDateTime.now(), 10, true);
        recorder.record("heartBeat", LocalDateTime.now(), 20, true);
        recorder.record("dailyReport", LocalDateTime.now(), 5, true);

        List<Map<String, Object>> stats = recorder.stats();

        assertThat(stats).hasSize(2);
        assertThat(stats.get(0).get("任务名")).isEqualTo("dailyReport");
        assertThat(stats.get(1).get("任务名")).isEqualTo("heartBeat");

        Map<String, Object> heartBeat = stats.get(1);
        assertThat(heartBeat.keySet()).containsExactly("任务名", "执行次数", "最近执行", "平均耗时ms", "执行线程");
        assertThat(heartBeat.get("执行次数")).isEqualTo(2);
        assertThat(heartBeat.get("平均耗时ms")).isEqualTo(15L);
        assertThat((String) heartBeat.get("最近执行")).matches("\\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    @DisplayName("只保留最近 200 条：录 210 条后容量为 200，首条 id 为 210，末条 id 为 11")
    void keepsAtMostCapacity() {
        for (int i = 0; i < 210; i++) {
            recorder.record("flood", LocalDateTime.now(), i, true);
        }

        List<TaskExecutionRecorder.Execution> all = recorder.recent(1000);

        assertThat(all).hasSize(200);
        assertThat(all.get(0).id()).isEqualTo(210);
        assertThat(all.get(199).id()).isEqualTo(11);
    }
}
