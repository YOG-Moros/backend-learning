package com.example.config.controller;

import com.example.config.model.ConfigResponse;
import com.example.config.properties.AppProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/config")
public class ConfigController {
    @Value("8081")
    private Integer serverPort;

    @Value("${spring.application.name}")
    private String appName;

    @Value("陈昶好")
    private String myName;

    @Value("学生")
    private String myJob;

    @GetMapping("/basic")
    public String getBasicConfig() {
        return "服务器端口为："+this.serverPort+"，应用名为："+appName;
    }

    @GetMapping("/my")
    public String getMyConfig() {
        return "我的姓名为：" + this.myName + "，职业为：" + this.myJob;
    }

    private final AppProperties appProperties;
    private final Environment environment;

    public ConfigController(AppProperties appProperties, Environment environment) {
        this.appProperties = appProperties;
        this.environment = environment;
    }

    @GetMapping
    public ConfigResponse currentConfiguration() {
        return new ConfigResponse(
                "02-config",
                appProperties.getName(),
                appProperties.getEnvironment(),
                appProperties.isFeaturesEnabled(),
                appProperties.getRequestTimeout().toMillis(),
                appProperties.getAllowedOrigins(),
                Arrays.asList(environment.getActiveProfiles())
        );
    }
}
