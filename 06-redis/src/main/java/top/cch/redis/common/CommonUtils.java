package top.cch.redis.common;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用工具方法：手机号校验、随机验证码
 *
 * @author cch
 */
public class CommonUtils {

    /**
     * 校验手机号：1 开头加 10 位数字，第二位限 3-9
     *
     * @param phone 电话
     * @return boolean
     */
    public static boolean checkPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return false;
        }
        String regex = "^1[3-9]\\d{9}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    /**
     * 生成 4 位随机验证码，范围 [1000, 9999]。
     * nextInt 的右边界是开区间，写 9999 会让 9999 永远取不到，这里要多加一个数
     *
     * @return int
     */
    public static int generateCode() {
        return ThreadLocalRandom.current().nextInt(1000, 10000);
    }
}
