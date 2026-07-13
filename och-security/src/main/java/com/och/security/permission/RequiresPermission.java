package com.och.security.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限要求注解
 * <p>标注在 Controller 方法或类上，声明该方法需要的权限标识。
 * 由 {@link PermissionInterceptor} 在请求预处理阶段进行校验。</p>
 *
 * <pre>
 * // 使用示例：
 * {@code @RequiresPermission(value = "call:skill:delete", description = "删除技能")}
 * public ResResult<Void> deleteSkill(@RequestParam Long id) {
 *     // ...
 * }
 * </pre>
 *
 * @author danmo
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {

    /**
     * 权限标识（如 call:skill:delete）
     */
    String value();

    /**
     * 权限描述（用于提示用户缺少什么权限）
     */
    String description() default "";
}
