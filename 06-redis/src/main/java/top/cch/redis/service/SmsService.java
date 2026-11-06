package top.cch.redis.service;

import top.cch.redis.dto.LoginRequest;
import top.cch.redis.dto.LoginResponse;

/**
 * 短信业务接口
 *
 * @author cch
 */
public interface SmsService {

    /**
     * 发送验证码短信。发送失败、校验不通过都通过 BusinessException 往外抛
     *
     * @param phone 手机号
     */
    void sendSms(String phone);

    /**
     * 验证码登录
     *
     * @param loginRequest 登录请求参数
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest);
}
