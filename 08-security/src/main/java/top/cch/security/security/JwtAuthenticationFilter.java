package top.cch.security.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import top.cch.security.entity.SecUser;
import top.cch.security.mapper.SecUserMapper;
import top.cch.security.model.LoginUser;
import top.cch.security.service.RoleService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cch
 * @date 2026/10/28
 * @description JwtAuthenticationFilter JWT 认证过滤器：
 *              解析 Bearer 令牌 → 回库确认用户有效 → 组装 LoginUser + 角色/权限点放进 SecurityContext
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final SecUserMapper userMapper;
    private final RoleService roleService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return LessonPaths.isLessonApi(request) || path.startsWith("/stage");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtUtils.parse(header.substring(7));
                if ("security-lesson".equals(claims.getIssuer())) {
                    chain.doFilter(request, response);
                    return;
                }
                SecUser user = userMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SecUser>()
                                .eq(SecUser::getUsername, claims.getSubject()));
                // 用户不存在或已停用：令牌无效，按未登录处理
                if (user != null && Boolean.TRUE.equals(user.getEnabled())) {
                    List<String> roles = roleService.loadRoleCodes(user.getId());
                    LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(),
                            user.getNickname(), roles, RolePermissions.permissionsOf(roles), user.getEnabled());
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    roles.forEach(code -> authorities.add(new SimpleGrantedAuthority("ROLE_" + code)));
                    loginUser.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(loginUser, null, authorities));
                }
            } catch (JwtException | IllegalArgumentException e) {
                // 令牌过期/伪造：不中断请求，交给后面的授权过滤器回 401
                log.debug("令牌解析失败：{}", e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }
}
