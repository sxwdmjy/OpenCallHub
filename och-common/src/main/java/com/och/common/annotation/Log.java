package com.och.common.annotation;

import com.och.common.enums.BusinessTypeEnum;
import com.och.common.enums.OperatorTypeEnum;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * <p>
 * 标注在 Controller 方法上，用于 AOP 切面自动记录操作日志。
 * 记录内容包括：操作模块、业务类型、操作人类别、是否保存请求参数等。
 * </p>
 *
 * @author danmo
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /** 模块标题（如：用户管理、菜单管理） */
    String title() default "";

    /** 业务类型 */
    BusinessTypeEnum businessType() default BusinessTypeEnum.OTHER;

    /** 操作人类别 */
    OperatorTypeEnum operatorType() default OperatorTypeEnum.MANAGE;

    /** 是否保存请求参数 */
    boolean isSaveRequestData() default true;

    /** 是否保存响应数据 */
    boolean isSaveResponseData() default true;
}