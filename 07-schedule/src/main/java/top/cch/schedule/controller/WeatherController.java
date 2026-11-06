package top.cch.schedule.controller;

import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.cch.schedule.service.DailyWeatherService;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class WeatherController {

    private final DailyWeatherService dailyWeatherService;

    @GetMapping("/weather")
    public Map<String, Object> weather() {
        Map<String, Object> result = new LinkedHashMap<>();
        JSONObject today = dailyWeatherService.getTodayWeather();
        if (today == null) {
            result.put("success", false);
            result.put("message", "获取天气数据失败");
            return result;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fxDate", today.getString("fxDate"));
        data.put("tempMax", today.getString("tempMax"));
        data.put("tempMin", today.getString("tempMin"));
        data.put("textDay", today.getString("textDay"));
        data.put("windDirDay", today.getString("windDirDay"));
        data.put("windScaleDay", today.getString("windScaleDay"));
        result.put("success", true);
        result.put("data", data);
        return result;
    }

    @PostMapping("/weather/send")
    public Map<String, Object> send() {
        Map<String, Object> result = new LinkedHashMap<>();
        boolean ok = dailyWeatherService.sendWeatherEmail();
        result.put("success", ok);
        result.put("message", ok ? "天气邮件已发送" : "天气邮件发送失败");
        return result;
    }
}
