package top.cch.zhihu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 知乎专题页面前后端交互 · 后端启动类
 *
 * @author mqxu
 */
@SpringBootApplication
@MapperScan("top.cch.zhihu.mapper")
public class ZhihuApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhihuApplication.class, args);
    }
}
