package top.cch.redis.service.impl;

import com.cloopen.rest.sdk.CCPRestSmsSDK;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.cch.redis.common.BusinessException;
import top.cch.redis.common.CommonUtils;
import top.cch.redis.common.ErrorCode;
import top.cch.redis.common.JwtUtils;
import top.cch.redis.common.RedisCache;
import top.cch.redis.common.RedisKeys;
import top.cch.redis.config.CloopenConfig;
import top.cch.redis.config.SmsProperties;
import top.cch.redis.dto.LoginRequest;
import top.cch.redis.dto.LoginResponse;
import top.cch.redis.service.SmsService;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信业务实现：验证码的发送、防刷、存储与登录校验
 *
 * @author cch
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    /**
     * 一天有多少秒，用于"当日发送条数"的计数窗口
     */
    private static final long SECONDS_PER_DAY = 24 * 60 * 60L;
    /**
     * 验证码 Hash 里存验证码的字段
     */
    private static final String FIELD_CODE = "code";
    /**
     * 验证码 Hash 里存状态的字段
     */
    private static final String FIELD_STATUS = "status";
    private static final String STATUS_UNUSED = "0";
    private static final String STATUS_USED = "1";

    private final CloopenConfig cloopenConfig;
    private final SmsProperties smsProperties;
    private final RedisCache redisCache;
    private final JwtUtils jwtUtils;
    private final CCPRestSmsSDK ccprRestSmsSDK;

    @Override
    public void sendSms(String phone) {
        // 1、检查 phone 格式是否正确
        if (!CommonUtils.checkPhone(phone)) {
            throw new BusinessException(ErrorCode.PHONE_ERROR);
        }

        // 2、发送间隔：SET NX EX 只让一次请求占住这个 key，占不到说明刚发过
        String intervalKey = RedisKeys.getSmsSendIntervalKey(phone);
        Boolean acquired = redisCache.setIfAbsent(intervalKey, 1, smsProperties.getSendIntervalSeconds());
        if (!Boolean.TRUE.equals(acquired)) {
            throw new BusinessException(ErrorCode.PHONE_CODE_SEND_TOO_MANY);
        }

        // 3、当日条数：计数器的过期时间在第一次自增时设置
        Long sentToday = redisCache.increment(RedisKeys.getSmsSendCountKey(phone), SECONDS_PER_DAY);
        if (sentToday != null && sentToday > smsProperties.getSendDailyLimit()) {
            throw new BusinessException(ErrorCode.PHONE_CODE_SEND_LIMIT);
        }

        // 4、调用工具方法，生成随机验证码
        int code = CommonUtils.generateCode();

        // 5、先发短信，成功之后再写 Redis：用户没收到短信时不留验证码占位
        boolean sent;
        try {
            sent = send(phone, code);
        } catch (RuntimeException ex) {
            redisCache.delete(intervalKey);
            throw new BusinessException(ErrorCode.PHONE_CODE_SEND_FAILED);
        }
        if (!sent) {
            // 发送失败，把冷却的 key 撤掉，允许立刻重试
            redisCache.delete(intervalKey);
            throw new BusinessException(ErrorCode.PHONE_CODE_SEND_FAILED);
        }

        // 6、验证码与状态一起存成 Hash，方便区分"没用过"和"已经用过了"
        Map<String, Object> value = new HashMap<>();
        value.put(FIELD_CODE, String.valueOf(code));
        value.put(FIELD_STATUS, STATUS_UNUSED);
        redisCache.hMSet(RedisKeys.getSmsKey(phone), value, smsProperties.getCodeExpireSeconds());
        redisCache.delete(RedisKeys.getSmsFailCountKey(phone));

        log.info("验证码已下发，手机号 {}", mask(phone));
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String phone = loginRequest.getPhone();
        String inputCode = loginRequest.getCode();

        // 1、校验手机号格式
        if (!CommonUtils.checkPhone(phone)) {
            throw new BusinessException(ErrorCode.PHONE_ERROR);
        }

        // 2、校验验证码是否为空
        if (inputCode == null || inputCode.isBlank()) {
            throw new BusinessException(ErrorCode.PHONE_CODE_EMPTY);
        }

        long result = redisCache.consumeSms(RedisKeys.getSmsKey(phone),
                RedisKeys.getSmsFailCountKey(phone), inputCode,
                smsProperties.getMaxFailCount(), smsProperties.getCodeExpireSeconds());
        if (result == -1) throw new BusinessException(ErrorCode.PHONE_CODE_EXPIRED);
        if (result == -2) throw new BusinessException(ErrorCode.PHONE_CODE_USED);
        if (result == -3) throw new BusinessException(ErrorCode.PHONE_CODE_ERROR_TOO_MANY);
        if (result != 1) throw new BusinessException(ErrorCode.PHONE_CODE_ERROR);

        // 7、签发 token 并返回登录信息
        String token = jwtUtils.create(phone);

        log.info("手机号 {} 登录成功", mask(phone));
        return new LoginResponse(token, phone, jwtUtils.getExpireSeconds());
    }

    /**
     * 调用短信通道。mockEnabled 为 true 时走模拟通道，验证码只写日志，不真正发送
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 是否发送成功
     */
    private String mask(String phone) { return phone.substring(0, 3) + "****" + phone.substring(7); }

    private boolean send(String phone, int code) {
        if (smsProperties.isMockEnabled()) {
            log.info("短信通道为模拟模式，手机号 {}，验证码 {}", mask(phone), code);
            return true;
        }

        // 注意 SDK 参数顺序：第一个是手机号，第二个是模板 ID
        String[] datas = {String.valueOf(code), "5"};
        HashMap<String, Object> result =
                ccprRestSmsSDK.sendTemplateSMS(phone, cloopenConfig.getTemplateId(), datas);

        if ("000000".equals(result.get("statusCode"))) {
            log.info("短信发送成功，手机号 {}", mask(phone));
            return true;
        }
        log.warn("短信发送失败，手机号 {}，状态码 {}，状态信息 {}",
                mask(phone), result.get("statusCode"), result.get("statusMsg"));
        return false;
    }
}
