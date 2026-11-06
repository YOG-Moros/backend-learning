package top.cch.security.sms;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.*;

@RequiredArgsConstructor
public class SmsCodeAuthenticationProvider implements AuthenticationProvider {
    private final SmsCodeService codes;
    private final UserDetailsService users;
    @Override public Authentication authenticate(Authentication authentication) {
        String phone = authentication.getName();
        codes.verify(phone, (String) authentication.getCredentials());
        UserDetails user = users.loadUserByUsername(phone);
        if (!user.isEnabled()) throw new DisabledException("账号已被禁用");
        if (!user.isAccountNonLocked()) throw new LockedException("账号已被锁定");
        if (!user.isAccountNonExpired()) throw new AccountExpiredException("账号已过期");
        if (!user.isCredentialsNonExpired()) throw new CredentialsExpiredException("凭证已过期");
        return new SmsCodeAuthenticationToken(user, user.getAuthorities());
    }
    @Override public boolean supports(Class<?> type) { return SmsCodeAuthenticationToken.class.isAssignableFrom(type); }
}
