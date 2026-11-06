package top.cch.schedule.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/**
 * 每日天气邮件：定时请求和风天气 3 天预报，取今天的数据发到指定邮箱
 *
 * @author cch
 */
@Slf4j
@Service
public class DailyWeatherService {

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    private static final String SUCCESS_CODE = "200";

    @Resource
    private JavaMailSender mailSender;

    @Value("${weather.api.key}")
    private String weatherApiKey;

    @Value("${weather.api.city}")
    private String cityId;

    @Value("${weather.api.url}")
    private String weatherApiUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${email.recipients}")
    private String recipients;

    @Value("${cch.schedule.mock-enabled:true}")
    private boolean mockEnabled;

    /**
     * 指定发送时间规则：每天 08:28:00 执行一次
     */
    @Scheduled(cron = "${cch.schedule.weather:0 28 8 * * ?}")
    public void sendDailyWeather() {
        try {
            String weatherResponse = getWeatherData();
            if (weatherResponse == null) {
                log.error("【天气邮件】和风天气接口返回为空");
                return;
            }

            JSONObject weatherJson = JSONObject.parseObject(weatherResponse);
            if (!SUCCESS_CODE.equals(weatherJson.getString("code"))) {
                log.error("【天气邮件】接口状态码 {}，{}", weatherJson.getString("code"), weatherJson);
                return;
            }

            JSONArray daily = weatherJson.getJSONArray("daily");
            if (daily == null || daily.isEmpty()) {
                log.error("【天气邮件】预报数据为空");
                return;
            }

            JSONObject today = daily.getJSONObject(0);
            String subject = "每日天气-" + LocalDate.now();
            String content = """
                    【今日天气】%s

                    白天：%s      夜间：%s
                    气温：%s℃ ~ %s℃
                    风向风力：%s %s 级
                    湿度：%s%%
                    日出 / 日落：%s / %s

                    数据更新时间：%s
                    本邮件由 08-schedule 定时任务自动发送
                    软件2412 陈昶好
                    """.formatted(
                    today.getString("fxDate"),
                    today.getString("textDay"),
                    today.getString("textNight"),
                    today.getString("tempMin"),
                    today.getString("tempMax"),
                    today.getString("windDirDay"),
                    today.getString("windScaleDay"),
                    today.getString("humidity"),
                    today.getString("sunrise"),
                    today.getString("sunset"),
                    weatherJson.getString("updateTime"));

            sendEmail(subject, content);
            log.info("【天气邮件】发送成功，收件人 {}，主题 {}", recipients, subject);
        } catch (Exception e) {
            log.error("【天气邮件】执行失败：{}", e.getMessage(), e);
        }
    }

    public JSONObject getTodayWeather() {
        try {
            String weatherResponse = getWeatherData();
            if (weatherResponse == null) {
                log.error("【天气查询】和风天气接口返回为空");
                return null;
            }

            JSONObject weatherJson = JSONObject.parseObject(weatherResponse);
            if (!SUCCESS_CODE.equals(weatherJson.getString("code"))) {
                log.error("【天气查询】接口状态码 {}，{}", weatherJson.getString("code"), weatherJson);
                return null;
            }

            JSONArray daily = weatherJson.getJSONArray("daily");
            if (daily == null || daily.isEmpty()) {
                log.error("【天气查询】预报数据为空");
                return null;
            }
            return daily.getJSONObject(0);
        } catch (Exception e) {
            log.error("【天气查询】获取天气数据失败：{}", e.getMessage(), e);
            return null;
        }
    }

    public boolean sendWeatherEmail() {
        JSONObject today = getTodayWeather();
        if (today == null) {
            log.error("【天气早报】获取天气数据失败");
            return false;
        }
        try {
            String subject = "每日天气-" + LocalDate.now();
            String content = """
                    【今日天气】%s

                    白天：%s
                    气温：%s℃ ~ %s℃
                    风向风力：%s %s 级

                    本邮件由 08-schedule 天气任务发送
                    软件2412 陈昶好
                    """.formatted(
                    today.getString("fxDate"),
                    today.getString("textDay"),
                    today.getString("tempMin"),
                    today.getString("tempMax"),
                    today.getString("windDirDay"),
                    today.getString("windScaleDay"));

            sendEmail(subject, content);
            log.info("【天气早报】发送成功，收件人 {}，主题 {}", recipients, subject);
            return true;
        } catch (Exception e) {
            log.error("【天气早报】执行失败：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 请求和风天气 3 天预报接口，返回原始 JSON 字符串
     *
     * @return 接口原始响应，请求失败时返回 null
     */
    private String getWeatherData() throws IOException {
        if (mockEnabled) {
            return """
                    {"code":"200","updateTime":"%s","daily":[{"fxDate":"%s",
                    "textDay":"晴","textNight":"晴","tempMin":"18","tempMax":"26",
                    "windDirDay":"东风","windScaleDay":"2","humidity":"60","sunrise":"06:00","sunset":"18:00"}]}
                    """.formatted(LocalDate.now(), LocalDate.now());
        }
        String requestUrl = String.format("%s?location=%s&key=%s", weatherApiUrl, cityId, weatherApiKey);
        Request request = new Request.Builder().url(requestUrl).build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
            log.error("【天气邮件】接口请求失败，HTTP {}", response.code());
        }
        return null;
    }

    /**
     * 发送纯文本邮件
     *
     * @param subject 邮件主题
     * @param content 邮件正文
     */
    private void sendEmail(String subject, String content) {
        if (mockEnabled) {
            log.info("【天气邮件】模拟发送，主题 {}", subject);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipients.split(","));
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }
}
