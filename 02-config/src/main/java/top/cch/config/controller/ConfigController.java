package top.cch.config.controller;

import top.cch.config.properties.StudentProperties;
import top.cch.config.service.EnvService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {

    private final StudentProperties studentProperties;

    private final EnvService envService;

    @Value("${server.port}")
    private Integer serverPort;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${cch.name}")
    private String myName;

    @Value("${cch.job}")
    private String myJob;

    @Value("${app.author}")
    private String author;

    @Value("${app.remark:暂无备注}")
    private String remark;

    @Value("${random.uuid}")
    private String randomUuid;

    @Value("${random.int(1,100)}")
    private Integer randomInt;

    @Value("#{${student.age} >= 18 ? '成年' : '未成年'}")
    private String adult;

    @Value("${env.name}")
    private String envName;

    @Value("${env.description}")
    private String envDescription;

    @GetMapping("/basic")
    public String getBasicInfo() {
        return "服务器端口是：" + this.serverPort + "，应用名称是：" + appName;
    }

    @GetMapping("/my")
    public String getMyInfo() {
        return "我的姓名是：" + this.myName + "，职业是：" + myJob;
    }

    @GetMapping("/value")
    public Map<String, Object> getValueCases() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("randomInt", randomInt);
        result.put("author", author);
        result.put("remark", remark);
        result.put("randomUuid", randomUuid);
        result.put("adult", adult);
        return result;
    }

    @GetMapping("/env")
    public String getEnv() {
        return "当前环境：" + envName + "，" + envDescription + "；Profile Bean：" + envService.envInfo();
    }

    @GetMapping("/app")
    public StudentProperties getApp() {
        return studentProperties;
    }
}
