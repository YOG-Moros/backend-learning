package top.cch.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import top.cch.security.entity.SysUser;
import top.cch.security.mapper.SysUserMapper;

@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {
    private final SysUserMapper users;
    private final UserAssembler assembler;
    @Override
    public UserDetails loadUserByUsername(String username) {
        SysUser user = users.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) throw new UsernameNotFoundException("用户名或密码错误");
        return assembler.toUserDetails(user);
    }
}
