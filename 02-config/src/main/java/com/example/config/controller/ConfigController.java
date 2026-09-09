package com.example.config.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
