package com.och.common.enums;

/**
 * 操作人类别枚举
 * <p>用于 {@link com.och.common.annotation.Log} 注解，标识操作是由后台管理员还是前端用户触发。</p>
 *
 * @author danmo
 */
public enum OperatorTypeEnum {

    /** 后台管理员 */
    MANAGE,

    /** 前端用户（座席） */
    MOBILE
}