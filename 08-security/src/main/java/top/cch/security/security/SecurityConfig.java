package top.cch.security.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author cch
 * @date 2026/10/28
 * @description SecurityConfig 安全配置：
 *              粒度一 URL 规则（/admin/** 要 ROLE_ADMIN）；粒度二/三 在各接口上的 @PreAuthorize；
 *              粒度四 数据归属比对在 Service 层。无状态 JWT，关闭 CSRF，401/403 走 JSON 出口
 **/
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final JwtService lessonJwt;
    private final top.cch.security.service.DbUserDetailsService lessonUsers;

    @Bean
    @org.springframework.core.annotation.Order(5)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(org.springframework.security.config.Customizer.withDefaults())
                // 前后端分离 + 无状态令牌，不需要 CSRF
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 登录和连通性检查匿名可访问
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/api/**").permitAll()
                        .requestMatchers("/auth/login", "/public/**", "/api/auth/**", "/api/public/**", "/error").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/ops/**").hasAnyRole("ADMIN", "AUDITOR")
                        .requestMatchers("/api/audit/**").hasAllRoles("ADMIN", "AUDITOR")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/articles/**").hasAuthority("ARTICLE_DELETE")
                        // 粒度一：/admin/** 整段要求 ADMIN 角色
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 其余全部要求登录
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                // 自定义 JWT 过滤器放在用户名密码过滤器之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new LessonJwtAuthenticationFilter(lessonJwt, lessonUsers), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<JwtAuthenticationFilter> jwtRegistration() {
        var registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(jwtAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }
}
