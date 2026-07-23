package com.intellectual.aspect;

import com.intellectual.annotation.RequirePermission;
import com.intellectual.security.LoginUser;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 权限校验切面
 *
 * <p>拦截所有标注了 {@link RequirePermission} 的方法（或类），
 * 从安全上下文中获取当前用户，比对其拥有的权限集合与注解声明的所需权限。</p>
 *
 * <p>触发时机：方法执行前（{@code @Before}）</p>
 *
 * <p>匹配规则：</p>
 * <ul>
 *   <li>先取方法上的 {@code @RequirePermission}，若不存在再取类上的</li>
 *   <li>{@code Logical.AND}：用户必须同时具备所有权限</li>
 *   <li>{@code Logical.OR}：用户满足任一权限即可</li>
 * </ul>
 */
@Aspect
@Component
public class PermissionAspect {

    /**
     * 在标注了 @RequirePermission 的方法执行前进行权限校验
     *
     * @param joinPoint         切入点，用于获取方法上的注解作为回退
     * @param requirePermission 自动注入的注解实例（优先取方法级，不存在时取类级）
     * @throws AccessDeniedException 当前用户未登录或权限不足时抛出
     */
    @Before("@within(requirePermission) || @annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        // 若通过 @within 匹配到的是类级注解，尝试回退到方法级
        if (requirePermission == null) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            requirePermission = signature.getMethod().getAnnotation(RequirePermission.class);
        }
        if (requirePermission == null) {
            return;
        }

        String[] requiredPerms = requirePermission.value();
        if (requiredPerms.length == 0) {
            return;
        }

        // 获取当前登录用户
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            throw new AccessDeniedException("未登录");
        }

        Set<String> userPermissions = loginUser.getPermissions();
        if (userPermissions == null || userPermissions.isEmpty()) {
            throw new AccessDeniedException("权限不足");
        }

        // 根据逻辑关系判断权限
        boolean hasPermission;
        if (requirePermission.logical() == RequirePermission.Logical.AND) {
            // AND：所有权限都必须满足，任一不满足即失败
            hasPermission = true;
            for (String perm : requiredPerms) {
                if (!userPermissions.contains(perm)) {
                    hasPermission = false;
                    break;
                }
            }
        } else {
            // OR：满足任一权限即可
            hasPermission = false;
            for (String perm : requiredPerms) {
                if (userPermissions.contains(perm)) {
                    hasPermission = true;
                    break;
                }
            }
        }

        if (!hasPermission) {
            throw new AccessDeniedException(
                    "权限不足，需要: " + String.join(", ", requiredPerms));
        }
    }

    /** 从 SecurityContext 中获取当前登录用户 */
    private LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }
}
