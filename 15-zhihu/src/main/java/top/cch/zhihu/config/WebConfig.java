package top.cch.zhihu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置
 *
 * <p>前端在 http://localhost:5173，后端在 http://localhost:8015，
 * 协议 / 主机 / 端口任一不同即跨域，浏览器会拦截响应。这里统一放开。</p>
 *
 * <p>注意：allowCredentials(true) 不能和 allowedOrigins("*") 一起用，
 * 要用 allowedOriginPatterns。</p>
 *
 * @author mqxu
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
