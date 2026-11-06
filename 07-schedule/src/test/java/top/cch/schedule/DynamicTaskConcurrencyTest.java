package top.cch.schedule;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import top.cch.schedule.service.DynamicTaskService;
import top.cch.schedule.service.TaskExecutionRecorder;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DynamicTaskConcurrencyTest {
    @Test void concurrentRegistrationSchedulesOnlyOnceAndDestroyCancels() throws Exception {
        TaskScheduler scheduler = mock(TaskScheduler.class);
        ScheduledFuture<?> scheduled = mock(ScheduledFuture.class);
        doReturn(scheduled).when(scheduler).schedule(any(Runnable.class), any(Trigger.class));
        var service = new DynamicTaskService(scheduler, mock(TaskExecutionRecorder.class));
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(8)) {
            var results = new java.util.ArrayList<Future<Boolean>>();
            for (int i=0;i<8;i++) results.add(pool.submit(() -> {
                start.await();
                try { service.register("same", "0 * * * * *"); return true; }
                catch (IllegalArgumentException expected) { return false; }
            }));
            start.countDown();
            int count=0;
            for (var result:results) if (result.get(5, TimeUnit.SECONDS)) count++;
            assertThat(count).isEqualTo(1);
            verify(scheduler, times(1)).schedule(any(Runnable.class), any(Trigger.class));
        } finally { service.destroy(); }
        verify(scheduled).cancel(false);
        assertThat(service.runningNames()).isEmpty();
        assertThat(service.counterSnapshot()).isEmpty();
    }
}
