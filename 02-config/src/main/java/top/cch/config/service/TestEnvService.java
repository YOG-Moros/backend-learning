package top.cch.config.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class TestEnvService implements EnvService {

    @Override
    public String envInfo() {
        return "我是 test 环境专属的 Bean";
    }
}
