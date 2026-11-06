package top.cch.schedule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskExecutionRecorder recorder;

    private final AtomicInteger heartBeatTimes = new AtomicInteger();
    private final AtomicInteger cleanTimes = new AtomicInteger();
    private final AtomicInteger reportTimes = new AtomicInteger();

    @Scheduled(fixedRate = 3000, initialDelay = 1000)
    public void heartBeat() {
        LocalDateTime start = LocalDateTime.now();
        int times = heartBeatTimes.incrementAndGet();
        log.info("【fixedRate】心跳第 {} 次，线程 {}", times, Thread.currentThread().getName());
        recorder.record("heartBeat", start, 0, true);
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 2000)
    public void cleanExpiredData() {
        LocalDateTime start = LocalDateTime.now();
        long begin = System.currentTimeMillis();
        int times = cleanTimes.incrementAndGet();
        log.info("【fixedDelay】清理临时数据第 {} 次，线程 {}", times, Thread.currentThread().getName());
        recorder.record("cleanExpiredData", start, System.currentTimeMillis() - begin, true);
    }

    @Scheduled(cron = "0/10 * * * * ?")
    public void dailyReport() {
        LocalDateTime start = LocalDateTime.now();
        int times = reportTimes.incrementAndGet();
        log.info("【cron】生成统计报表第 {} 次，线程 {}", times, Thread.currentThread().getName());
        recorder.record("dailyReport", start, 0, true);
    }
}
