package com.och.common.utils;

/**
 * 字符串工具类
 * <p>封装常用的字符串空值判断操作，内部委托给 Apache Commons Lang3。
 * 提供统一的静态方法入口，便于项目内一致使用。</p>
 *
 * @author danmo
 * @date 2024-01-01
 */
public class StringUtils {

    /**
     * 判断字符串是否为空（null 或 空串）
     *
     * @param cs 待检查的字符序列
     * @return true 表示 null 或长度为 0
     */
    public static boolean isEmpty(CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isEmpty(cs);
    }

    /**
     * 判断字符串是否不为空
     *
     * @param cs 待检查的字符序列
     * @return true 表示非 null 且长度大于 0
     */
    public static boolean isNotEmpty(CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isNotEmpty(cs);
    }

    /**
     * 判断字符串是否为空白（null、空串或仅包含空白字符）
     *
     * @param cs 待检查的字符序列
     * @return true 表示为空白
     */
    public static boolean isBlank(CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isBlank(cs);
    }

    /**
     * 判断字符串是否不为空白
     *
     * @param cs 待检查的字符序列
     * @return true 表示非空白
     */
    public static boolean isNotBlank(CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(cs);
    }

    /**
     * 比较两个字符串是否相等（null-safe）
     *
     * @param cs1 字符串1
     * @param cs2 字符串2
     * @return true 表示相等
     */
    public static boolean equals(CharSequence cs1, CharSequence cs2) {
        return org.apache.commons.lang3.StringUtils.equals(cs1, cs2);
    }

    /**
     * 按指定字符串右侧补齐
     *
     * @param str 原始字符串
     * @param size 目标长度
     * @param padStr 补齐字符串
     * @return 补齐后的字符串
     */
    public static String rightPad(String str, int size, String padStr) {
        return org.apache.commons.lang3.StringUtils.rightPad(str, size, padStr);
    }
}
