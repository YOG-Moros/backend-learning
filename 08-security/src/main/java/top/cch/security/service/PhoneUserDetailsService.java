package top.cch.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.cch.security.config.SecurityProperties;
import top.cch.security.entity.*;
import top.cch.security.mapper.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhoneUserDetailsService implements UserDetailsService {
    private final SysUserMapper users;
    private final SysRoleMapper roles;
    private final UserAssembler assembler;
    private final SecurityProperties properties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDetails loadUserByUsername(String phone) {
        SysUser user = users.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));
        if (user == null) {
            if (!properties.getSms().isAutoRegister()) throw new UsernameNotFoundException("手机号未注册");
            user = new SysUser();
            user.setUsername("sms_" + UUID.randomUUID().toString().replace("-", ""));
            user.setPhone(phone); user.setNickname("短信用户"); user.setEnabled(true); user.setLocked(false);
            user.setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(UUID.randomUUID().toString()));
            users.insert(user);
            SysRole role = new SysRole(); role.setUserId(user.getId()); role.setRoleCode("USER"); role.setGrantType("ROLE");
            roles.insert(role);
        }
        return assembler.toUserDetails(user);
    }
}
