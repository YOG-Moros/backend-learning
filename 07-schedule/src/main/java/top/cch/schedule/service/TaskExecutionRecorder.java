package top.cch.schedule.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class TaskExecutionRecorder {

    private static final int CAPACITY = 200;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ConcurrentLinkedDeque<Execution> records = new ConcurrentLinkedDeque<>();

    private final AtomicLong seq = new AtomicLong();

    public void record(String taskName, LocalDateTime startTime, long costMs, boolean success) {
        records.addFirst(new Execution(
                seq.incrementAndGet(), taskName, startTime.format(FMT),
                Thread.currentThread().getName(), costMs, success));
        while (records.size() > CAPACITY) {
            records.pollLast();
        }
    }

    public List<Execution> recent(int limit) {
        return records.stream().limit(limit).toList();
    }

    public List<Map<String, Object>> stats() {
        Map<String, List<Execution>> grouped = records.stream()
                .collect(Collectors.groupingBy(Execution::taskName, LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> result = new ArrayList<>();
        grouped.forEach((name, list) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("任务名", name);
            item.put("执行次数", list.size());
            item.put("最近执行", list.get(0).time());
            item.put("平均耗时ms", Math.round(list.stream().mapToLong(Execution::costMs).average().orElse(0)));
            item.put("执行线程", list.get(0).thread());
            result.add(item);
        });
        result.sort(Comparator.comparing(m -> String.valueOf(m.get("任务名"))));
        return result;
    }

    public record Execution(long id, String taskName, String time, String thread, long costMs, boolean success) {
    }
}
