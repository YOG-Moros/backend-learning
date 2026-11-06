package top.cch.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import top.cch.security.common.BusinessException;
import top.cch.security.common.ErrorCode;
import top.cch.security.dto.LoginRequest;
import top.cch.security.entity.SecUser;
import top.cch.security.mapper.SecUserMapper;
import top.cch.security.model.LoginUser;
import top.cch.security.security.JwtProperties;
import top.cch.security.security.JwtUtils;
import top.cch.security.security.RolePermissions;
import top.cch.security.vo.LoginVO;
import top.cch.security.vo.MeVO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cch
 * @date 2026/10/28
 * @description AuthService 认证：用户名密码换 JWT（密码错 30001，停用 30002）。
 *              权限点不进令牌：令牌只放身份，权限每次请求由过滤器回库查，改角色立即生效
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SecUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final RoleService roleService;

    public LoginVO login(LoginRequest request) {
        SecUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SecUser>().eq(SecUser::getUsername, request.getUsername()));
        // 用户不存在和密码错误返回同一个错误码，避免暴露账号是否存在
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        List<String> roles = roleService.loadRoleCodes(user.getId());
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), user.getNickname(),
                roles, RolePermissions.permissionsOf(roles), user.getEnabled());
        log.info("用户 {} 登录成功，角色 {}", user.getUsername(), roles);
        return new LoginVO(jwtUtils.generate(loginUser), "Bearer",
                LocalDateTime.now().plusMinutes(jwtProperties.getExpireMinutes()),
                user.getUsername(), user.getNickname(), roles);
    }

    public MeVO me(LoginUser loginUser) {
        // authorities 与 getAuthorities() 保持一致：ROLE_ 前缀角色 + 权限点
        List<String> authorities = new ArrayList<>();
        loginUser.getRoles().forEach(role -> authorities.add("ROLE_" + role));
        authorities.addAll(loginUser.getPermissions());
        return new MeVO(loginUser.getUsername(), loginUser.getNickname(),
                loginUser.getRoles(), authorities, loginUser.getEnabled());
    }
}
