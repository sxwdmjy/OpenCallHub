package com.och.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全工具类基类
 * <p>提供通用的安全上下文相关静态方法基类，由 och-security 模块的子类扩展。</p>
 *
 * @author danmo
 * @date 2024-01-01
 */
public class SecurityUtils {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /**
     * 受保护的构造器，工具类不应实例化
     */
    protected SecurityUtils() {
    }

    /**
     * 使用 BCrypt 加密登录密码
     *
     * @param password 明文密码
     * @return BCrypt 密文
     */
    public static String encryptPassword(String password) {
        return PASSWORD_ENCODER.encode(password);
    }
}
