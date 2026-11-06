package top.cch.web.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import top.cch.web.filter.ApiKeyFilter;
import top.cch.web.filter.LogFilter;
import top.cch.web.filter.MyFilter;
import top.cch.web.filter.ParameterCleanFilter;
import top.cch.web.filter.RequestTimeFilter;
import top.cch.web.filter.TraceIdFilter;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<LogFilter> logFilterFilterRegistrationBean() {
        FilterRegistrationBean<LogFilter> registration = new FilterRegistrationBean<>(new LogFilter());
        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add("/api/log/hello");
        registration.setUrlPatterns(urlPatterns);
        registration.setOrder(2);
        registration.addInitParameter("exclusions", "*.js,*.css,*.png");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> bean = new FilterRegistrationBean<>(new TraceIdFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<ParameterCleanFilter> parameterCleanFilter() {
        FilterRegistrationBean<ParameterCleanFilter> bean =
                new FilterRegistrationBean<>(new ParameterCleanFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<ApiKeyFilter> apiKeyFilter() {
        FilterRegistrationBean<ApiKeyFilter> bean = new FilterRegistrationBean<>(new ApiKeyFilter());
        bean.addUrlPatterns("/demo/secure");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 30);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<RequestTimeFilter> requestTimeFilter() {
        FilterRegistrationBean<RequestTimeFilter> bean = new FilterRegistrationBean<>(new RequestTimeFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 40);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<MyFilter> myFilter() {
        FilterRegistrationBean<MyFilter> bean = new FilterRegistrationBean<>(new MyFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 50);
        return bean;
    }
}
