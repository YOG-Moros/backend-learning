package top.cch.schedule.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 每 3 秒触发一次的演示任务
 *
 * @author cch
 */
@Slf4j
@Service
public class ScheduleService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AtomicInteger intervalCount = new AtomicInteger();

    /**
     * 秒字段写 0/3，表示从第 0 秒起每 3 秒匹配一次（0、3、6 … 57 秒）
     */
    @Scheduled(cron = "${cch.schedule.interval:0/3 * * * * ?}")
    public void everyThreeSecondsTask() {
        log.info("[每3秒任务] 第 {} 次执行，当前时间：{}，执行线程：{}",
                intervalCount.incrementAndGet(),
                LocalDateTime.now().format(FORMATTER),
                Thread.currentThread().getName());
    }
}
