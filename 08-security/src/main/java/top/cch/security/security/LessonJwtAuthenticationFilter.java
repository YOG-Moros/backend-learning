package top.cch.security.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.web.filter.OncePerRequestFilter;
import top.cch.security.service.DbUserDetailsService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class LessonJwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final DbUserDetailsService users;
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        return !LessonPaths.isLessonApi(request);
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                var claims = jwt.parse(header.substring(7));
                UserDetails user = users.loadUserByUsername(claims.getSubject());
                if (user.isEnabled() && user.isAccountNonLocked() && user.isAccountNonExpired() && user.isCredentialsNonExpired()) {
                    List<org.springframework.security.core.GrantedAuthority> authorities = new ArrayList<>(user.getAuthorities());
                    List<?> issued = claims.get("authorities", List.class);
                    if (issued != null && issued.contains("FACTOR_PASSWORD")) authorities.add(new SimpleGrantedAuthority("FACTOR_PASSWORD"));
                    SecurityContextHolder.getContext().setAuthentication(
                            UsernamePasswordAuthenticationToken.authenticated(user, null, authorities));
                }
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
