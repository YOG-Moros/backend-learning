package top.cch.schedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务模块启动类
 * <p>
 * {@code @EnableScheduling} 开启定时任务支持，没有它 {@code @Scheduled} 不会生效。
 *
 * @author cch
 */
@EnableScheduling
@SpringBootApplication
public class ScheduleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduleApplication.class, args);
    }
}
