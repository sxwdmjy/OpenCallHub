package com.och.common.utils;


import cn.hutool.core.util.PhoneUtil;

/**
 * 手机号工具类
 * @author danmo
 * @date 2025/05/26 17:58
 */
public class PhoneUtils extends PhoneUtil {

    /**
     * 判断是否为手机号码
     * @param phone
     * @return
     */
    public static boolean isPhone(String phone){
        return PhoneUtil.isPhone(phone);
    }

    /**
     * 获取手机号码的区域
     * @param phone 手机号码
     * @return 区域
     */
    public static String getPhoneArea(String phone){
        if (isPhone(phone)){
            return phone.substring(0, 7);
        }
        return null;
    }
}
