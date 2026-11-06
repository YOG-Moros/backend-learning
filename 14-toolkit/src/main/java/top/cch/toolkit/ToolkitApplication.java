package top.cch.toolkit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 工具库实战模块启动类
 *
 * @author mqxu
 * @date 2026-10-22
 * @description Hutool 典型功能的工程化案例
 */
@SpringBootApplication
public class ToolkitApplication {

    public static void main(String[] args) {
        // 画验证码、二维码、海报都依赖 AWT，服务器上没有显示设备，必须 headless
        System.setProperty("java.awt.headless", "true");
        SpringApplication.run(ToolkitApplication.class, args);
    }
}
