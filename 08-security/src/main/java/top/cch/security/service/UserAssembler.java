package top.cch.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import top.cch.security.entity.*;
import top.cch.security.mapper.SysRoleMapper;

@Service
@RequiredArgsConstructor
public class UserAssembler {
    private final SysRoleMapper roles;
    public UserDetails toUserDetails(SysUser user) {
        String[] authorities = roles.selectList(new LambdaQueryWrapper<SysRole>().eq(SysRole::getUserId, user.getId()))
                .stream().map(SysRole::toAuthority).toArray(String[]::new);
        return User.withUsername(user.getUsername()).password(user.getPassword()).authorities(authorities)
                .disabled(!Boolean.TRUE.equals(user.getEnabled())).accountLocked(Boolean.TRUE.equals(user.getLocked())).build();
    }
}
