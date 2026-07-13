package com.och.security.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计注解
 * <p>标注在需要审计的方法上，由 {@link AuditAspect} 切面自动记录操作日志。
 * 记录内容包括操作人、目标类型、请求参数、IP、耗时等。</p>
 *
 * @author danmo
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    /**
     * 操作类型（如 CREATE, UPDATE, DELETE, LOGIN）
     */
    String action();

    /**
     * 目标类型（如 USER, ROLE, CALL）
     */
    String targetType() default "";

    /**
     * 操作描述
     */
    String description() default "";
}
