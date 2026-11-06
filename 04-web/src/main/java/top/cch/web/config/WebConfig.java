package top.cch.web.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.cch.web.interceptor.CallbackTraceInterceptor;
import top.cch.web.interceptor.TimeStatInterceptor;
import top.cch.web.interceptor.TokenInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final TimeStatInterceptor timeStatInterceptor;
    private final CallbackTraceInterceptor callbackTraceInterceptor;
    private final TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(timeStatInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/static/**", "/error")
                .order(1);

        registry.addInterceptor(callbackTraceInterceptor)
                .addPathPatterns("/demo/callback")
                .order(2);

        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/demo/token")
                .order(3);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
