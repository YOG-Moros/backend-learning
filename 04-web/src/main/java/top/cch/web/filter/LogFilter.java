package top.cch.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class LogFilter implements Filter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("LogFilter 过滤器初始化");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String method = req.getMethod();
        String uri = req.getRequestURI();
        Map<String, String[]> parameterMap = req.getParameterMap();
        String params = parameterMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
                .collect(Collectors.joining("&"));
        log.info("【过滤器前置】请求方法是：{}，请求地址是：{}，请求参数是：{}，请求时间是：{}",
                method, uri, params, LocalDateTime.now().format(FORMATTER));
        chain.doFilter(request, response);
        log.info("【过滤器后置】chain.doFilter() 返回：拦截器与 Controller 已全部执行完毕，响应即将写回客户端");
    }

    @Override
    public void destroy() {
        log.info("LogFilter 过滤器销毁");
    }
}
