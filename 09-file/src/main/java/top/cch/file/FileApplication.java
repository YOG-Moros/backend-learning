package top.cch.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 文件上传模块启动类
 *
 * @author cch
 * @date 2026/11/4
 * @description 阿里云 OSS 图片上传
 */
@SpringBootApplication
public class FileApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }
}
