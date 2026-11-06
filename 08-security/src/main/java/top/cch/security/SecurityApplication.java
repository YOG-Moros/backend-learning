package top.cch.security;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author cch
 * @date 2026/10/28
 * @description SecurityApplication 实训任务提交与批改系统启动类
 **/
@SpringBootApplication
@MapperScan("top.cch.security.mapper")
public class SecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityApplication.class, args);
    }
}
