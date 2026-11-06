package top.cch.security.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.*;
import org.springframework.web.cors.*;
import top.cch.security.service.*;
import top.cch.security.security.*;
import top.cch.security.sms.*;
import java.util.List;

@Configuration
public class LessonSecurityConfig {
    @Bean("lessonAuthenticationManager")
    public AuthenticationManager authenticationManager(DbUserDetailsService users, PhoneUserDetailsService phones, SmsCodeService codes) {
        DaoAuthenticationProvider password = new DaoAuthenticationProvider(users);
        password.setPasswordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());
        return new ProviderManager(password, new SmsCodeAuthenticationProvider(codes, phones));
    }

    private HttpSecurity basic(HttpSecurity http, String path, AuthenticationManager manager,
                               RestAuthenticationEntryPoint entry, RestAccessDeniedHandler denied) {
        return http.securityMatcher(path).authenticationManager(manager)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(c -> c.disable()).httpBasic(b -> b.authenticationEntryPoint(entry))
                .exceptionHandling(e -> e.authenticationEntryPoint(entry).accessDeniedHandler(denied));
    }

    @Bean @Order(1)
    public SecurityFilterChain stage1(HttpSecurity http, @Qualifier("lessonAuthenticationManager") AuthenticationManager manager,
                                     RestAuthenticationEntryPoint entry, RestAccessDeniedHandler denied) throws Exception {
        return basic(http, "/stage1/**", manager, entry, denied)
                .authorizeHttpRequests(a -> a.anyRequest().authenticated()).build();
    }
    @Bean @Order(2)
    public SecurityFilterChain stage2(HttpSecurity http, @Qualifier("lessonAuthenticationManager") AuthenticationManager manager,
                                     RestAuthenticationEntryPoint entry, RestAccessDeniedHandler denied) throws Exception {
        return basic(http, "/stage2/**", manager, entry, denied).authorizeHttpRequests(a -> a
                .requestMatchers("/stage2/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/stage2/articles/**").permitAll()
                .anyRequest().authenticated()).build();
    }
    @Bean @Order(3)
    public SecurityFilterChain stage3(HttpSecurity http, @Qualifier("lessonAuthenticationManager") AuthenticationManager manager,
                                     RestAuthenticationEntryPoint entry, RestAccessDeniedHandler denied) throws Exception {
        return basic(http, "/stage3/**", manager, entry, denied).authorizeHttpRequests(a -> a
                .requestMatchers("/stage3/admin/**").hasRole("ADMIN")
                .requestMatchers("/stage3/ops/**").hasAnyRole("ADMIN", "AUDITOR")
                .requestMatchers("/stage3/audit/**").hasAllRoles("ADMIN", "AUDITOR")
                .requestMatchers(HttpMethod.DELETE, "/stage3/articles/**").hasAuthority("ARTICLE_DELETE")
                .anyRequest().authenticated()).build();
    }
    @Bean @Order(4)
    public SecurityFilterChain stage4(HttpSecurity http, @Qualifier("lessonAuthenticationManager") AuthenticationManager manager) throws Exception {
        return http.securityMatcher("/stage4/**").authenticationManager(manager)
                .authorizeHttpRequests(a -> a.requestMatchers("/stage4/login.html", "/stage4/login", "/stage4/csrf").permitAll()
                        .anyRequest().authenticated())
                .formLogin(f -> f.loginPage("/stage4/login.html").loginProcessingUrl("/stage4/login")
                        .defaultSuccessUrl("/stage4/me", false).failureUrl("/stage4/login.html?error").permitAll())
                .logout(l -> l.logoutUrl("/stage4/logout").logoutSuccessUrl("/stage4/login.html?logout")
                        .invalidateHttpSession(true).deleteCookies("JSESSIONID"))
                .csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())).build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource(SecurityProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(properties.getCors().getAllowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "X-XSRF-TOKEN"));
        config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        config.setAllowCredentials(false); config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
