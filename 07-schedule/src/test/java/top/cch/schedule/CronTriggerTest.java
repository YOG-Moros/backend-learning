package top.cch.schedule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CronTriggerTest {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private Instant next(String cron, Instant last) {
        SimpleTriggerContext context = new SimpleTriggerContext(last, last, last);
        return new CronTrigger(cron, ZONE).nextExecution(context);
    }

    @Test
    @DisplayName("每天 9:22 触发：当天上午 10 点之后，下一次是第二天 9:22")
    void dailyAtNineTwentyTwo() {
        LocalDate today = LocalDate.now();
        Instant last = today.atTime(10, 0, 0).atZone(ZONE).toInstant();

        ZonedDateTime next = next("0 22 9 * * ?", last).atZone(ZONE);

        assertThat(next.toLocalDate()).isEqualTo(today.plusDays(1));
        assertThat(next.getHour()).isEqualTo(9);
        assertThat(next.getMinute()).isEqualTo(22);
        assertThat(next.getSecond()).isZero();
    }

    @Test
    @DisplayName("每 10 秒触发：下一次对齐自然时间的整 10 秒")
    void everyTenSecondsAlignsToWallClock() {
        Instant last = LocalDateTime.of(2026, 11, 4, 23, 15, 35, 449_000_000).atZone(ZONE).toInstant();

        ZonedDateTime next = next("0/10 * * * * ?", last).atZone(ZONE);

        assertThat(next.getSecond()).isEqualTo(40);
        assertThat(next.getNano()).isZero();
    }

    @Test
    @DisplayName("工作日 9 点触发：2026-11-06 是周五，周五 9 点之后下一次是下周一 9 点")
    void weekdayNineFromFriday() {
        Instant last = LocalDateTime.of(2026, 11, 6, 9, 0, 0).atZone(ZONE).toInstant();

        ZonedDateTime next = next("0 0 9 ? * MON-FRI", last).atZone(ZONE);

        assertThat(next.toLocalDate()).isEqualTo(LocalDate.of(2026, 11, 9));
        assertThat(next.getDayOfWeek().getValue()).isEqualTo(1);
        assertThat(next.getHour()).isEqualTo(9);
    }

    @Test
    @DisplayName("五位表达式构造时立即抛出 IllegalArgumentException")
    void invalidExpressionFailsFast() {
        assertThatThrownBy(() -> new CronTrigger("0/10 * * * *"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
