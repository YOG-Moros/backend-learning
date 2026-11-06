package top.cch.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@WebFilter(urlPatterns = "/demo/annotation/hello")
public class AnnotationDemoFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        log.info("AnnotationDemoFilter（@WebFilter 注册）放行前：{} {}",
                request.getMethod(), request.getRequestURI());
        chain.doFilter(request, response);
        log.info("AnnotationDemoFilter（@WebFilter 注册）执行完毕：{} {}",
                request.getMethod(), request.getRequestURI());
    }
}
