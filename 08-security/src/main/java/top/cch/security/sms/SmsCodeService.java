package top.cch.security.sms;
public interface SmsCodeService {
    String sendCode(String phone);
    void verify(String phone, String code);
}
