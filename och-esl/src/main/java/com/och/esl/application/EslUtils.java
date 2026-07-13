package com.och.esl.application;

/**
 * ESL 模块通用工具方法。
 * <p>提供号码脱敏等跨类共享的纯函数，避免代码重复。</p>
 *
 * @author danmo
 */
public final class EslUtils {

    private EslUtils() {
        /* 工具类不可实例化 */
    }

    /**
     * 对手机号码进行脱敏处理，仅显示前 3 位和后 4 位，中间用星号替换。
     * <p>短号码（少于 7 位）不做脱敏直接返回，如分机号、内线短号等。</p>
     *
     * @param phone 原始号码（可为 null）
     * @return 脱敏后的号码，null 返回 null
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
