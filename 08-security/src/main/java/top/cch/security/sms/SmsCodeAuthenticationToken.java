package top.cch.security.sms;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

public class SmsCodeAuthenticationToken extends AbstractAuthenticationToken {
    private final Object principal;
    private Object credentials;
    public SmsCodeAuthenticationToken(String phone, String code) {
        super((Collection<? extends GrantedAuthority>) null);
        principal = phone; credentials = code;
    }
    public SmsCodeAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities); this.principal = principal; super.setAuthenticated(true);
    }
    @Override public Object getPrincipal() { return principal; }
    @Override public Object getCredentials() { return credentials; }
    @Override public void eraseCredentials() { super.eraseCredentials(); credentials = null; }
    @Override public void setAuthenticated(boolean authenticated) {
        if (authenticated) throw new IllegalArgumentException("只能由认证提供者建立可信凭证");
        super.setAuthenticated(false);
    }
}
