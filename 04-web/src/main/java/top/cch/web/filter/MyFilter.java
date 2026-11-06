package top.cch.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class MyFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        log.info("执行 MyFilter 的过滤逻辑：{} {}", request.getMethod(), request.getRequestURI());
        chain.doFilter(request, response);
        log.info("接口访问完毕，回到 MyFilter：{} {}", request.getMethod(), request.getRequestURI());
    }
}
