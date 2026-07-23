package com.intellectual.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解 —— 声明接口所需的权限标识
 *
 * <p>可用于方法或类级别。方法上的注解优先级高于类上的注解。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 需要单个权限
 * &#64;RequirePermission("system:user:delete")
 * &#64;DeleteMapping("/{id}")
 * public Result deleteUser(...) { }
 *
 * // 需要同时满足多个权限（AND）
 * &#64;RequirePermission(value = {"system:user:add", "system:user:edit"}, logical = Logical.AND)
 *
 * // 满足任一权限即可（OR）
 * &#64;RequirePermission(value = {"system:user:view", "system:role:view"}, logical = Logical.OR)
 * </pre>
 *
 * <p>权限标识来自 {@code sys_menu.perms} 字段，由 {@code PermissionAspect} 切面在方法执行前校验</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /** 需要的权限标识数组 */
    String[] value() default {};

    /** 多权限之间的逻辑关系，默认为 AND（必须全部满足） */
    Logical logical() default Logical.AND;

    enum Logical {
        /** 所有权限都必须具备 */
        AND,
        /** 满足任一权限即可 */
        OR
    }
}
