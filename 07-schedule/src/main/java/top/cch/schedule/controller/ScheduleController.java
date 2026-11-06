package top.cch.schedule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.cch.schedule.service.DynamicTaskService;
import top.cch.schedule.service.TaskExecutionRecorder;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final TaskExecutionRecorder recorder;
    private final DynamicTaskService dynamicTaskService;

    @GetMapping("/tasks")
    public Map<String, Object> tasks() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("统计", recorder.stats());
        return result;
    }

    @GetMapping("/tasks/records")
    public Map<String, Object> records(@RequestParam(defaultValue = "12") int limit) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("记录", recorder.recent(limit));
        return result;
    }

    @PostMapping("/tasks/dynamic")
    public Map<String, Object> register(@RequestParam String name, @RequestParam String cron) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            result.put("success", true);
            result.put("运行中", dynamicTaskService.register(name, cron));
            result.put("cron", cron);
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/tasks/dynamic")
    public Map<String, Object> dynamic() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("运行中", dynamicTaskService.runningNames());
        result.put("各任务执行次数", dynamicTaskService.counterSnapshot());
        return result;
    }

    @DeleteMapping("/tasks/dynamic/{name}")
    public Map<String, Object> cancel(@PathVariable String name) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (dynamicTaskService.cancel(name)) {
            result.put("success", true);
            result.put("message", "已取消");
        } else {
            result.put("success", false);
            result.put("message", "任务不存在");
        }
        return result;
    }
}
