package top.cch.redis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import top.cch.redis.config.DemoProperties;

@SpringBootApplication
@MapperScan("top.cch.redis.mapper")
@EnableConfigurationProperties(DemoProperties.class)
public class RedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisApplication.class, args);
    }
}
