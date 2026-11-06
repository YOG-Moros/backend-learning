package top.cch.security.sms;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SmsCodeAuthenticationProviderTest {
    @Test void disabledAndLockedAccountsCannotBypassPasswordProvider() {
        var codes = mock(SmsCodeService.class);
        var users = mock(UserDetailsService.class);
        var provider = new SmsCodeAuthenticationProvider(codes, users);
        when(users.loadUserByUsername("13900001111")).thenReturn(User.withUsername("dave").password("unused").roles("USER").disabled(true).build());
        assertThatThrownBy(() -> provider.authenticate(new SmsCodeAuthenticationToken("13900001111", "000001")))
                .isInstanceOf(DisabledException.class);
        when(users.loadUserByUsername("13900001111")).thenReturn(User.withUsername("frank").password("unused").roles("USER").accountLocked(true).build());
        assertThatThrownBy(() -> provider.authenticate(new SmsCodeAuthenticationToken("13900001111", "000001")))
                .isInstanceOf(LockedException.class);
    }
}
