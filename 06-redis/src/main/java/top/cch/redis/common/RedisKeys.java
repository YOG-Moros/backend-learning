package top.cch.redis.common;

/**
 * Redis key 命名，统一收口，避免各处手拼字符串不一致
 *
 * @author cch
 */
public class RedisKeys {

    /**
     * 验证码 key，存 Hash：code 是验证码，status 是 0 未使用 / 1 已使用
     */
    public static String getSmsKey(String phone) {
        return "sms:captcha:" + phone;
    }

    /**
     * 发送间隔 key，存在即代表还在冷却中
     */
    public static String getSmsSendIntervalKey(String phone) {
        return "sms:send:interval:" + phone;
    }

    /**
     * 当日发送次数 key
     */
    public static String getSmsSendCountKey(String phone) {
        return "sms:send:count:" + phone;
    }

    /**
     * 验证码连续输错次数 key
     */
    public static String getSmsFailCountKey(String phone) {
        return "sms:fail:" + phone;
    }
}
