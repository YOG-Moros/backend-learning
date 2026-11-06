package top.cch.schedule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicTaskService {

    private final TaskScheduler taskScheduler;
    private final TaskExecutionRecorder recorder;

    private final Map<String, ScheduledFuture<?>> running = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    public synchronized Set<String> register(String name, String cron) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("任务名不能为空");
        if (running.containsKey(name)) {
            throw new IllegalArgumentException("任务已存在：" + name);
        }
        CronTrigger trigger = new CronTrigger(cron);
        AtomicInteger counter = counters.computeIfAbsent(name, k -> new AtomicInteger());

        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
            LocalDateTime start = LocalDateTime.now();
            long begin = System.currentTimeMillis();
            int times = counter.incrementAndGet();
            log.info("【动态任务】{} 第 {} 次执行，线程 {}", name, times, Thread.currentThread().getName());
            recorder.record(name, start, System.currentTimeMillis() - begin, true);
        }, trigger);

        if (future == null) {
            counters.remove(name);
            throw new IllegalArgumentException("cron 没有未来触发时间");
        }
        running.put(name, future);
        log.info("动态任务已注册：{}，cron={}", name, cron);
        return Set.copyOf(running.keySet());
    }

    public synchronized boolean cancel(String name) {
        ScheduledFuture<?> future = running.remove(name);
        if (future == null) {
            return false;
        }
        future.cancel(false);
        counters.remove(name);
        log.info("动态任务已取消：{}", name);
        return true;
    }

    public Set<String> runningNames() {
        return Set.copyOf(running.keySet());
    }

    public Map<String, Integer> counterSnapshot() {
        Map<String, Integer> result = new LinkedHashMap<>();
        counters.forEach((name, counter) -> result.put(name, counter.get()));
        return result;
    }

    @jakarta.annotation.PreDestroy
    public synchronized void destroy() {
        running.values().forEach(future -> future.cancel(false));
        running.clear();
        counters.clear();
    }
}
