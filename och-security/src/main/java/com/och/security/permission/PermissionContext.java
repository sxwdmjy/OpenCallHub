package com.och.security.permission;

import com.och.security.authority.OchGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 权限上下文
 * <p>提供便捷的静态方法用于获取当前认证信息及权限校验。</p>
 *
 * @author danmo
 */
public class PermissionContext {

    private PermissionContext() {
    }

    /**
     * 获取当前认证信息
     *
     * @return Spring Security 认证对象，未认证时返回 null
     */
    public static Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 检查当前用户是否拥有指定权限
     * <p>
     * 遍历用户的所有角色（OchGrantedAuthority），若任一角色为 ROLE_ADMIN（超级管理员）
     * 或任一角色的权限列表中包含目标权限，则返回 true。
     * </p>
     *
     * @param permission 权限标识（如 call:skill:delete）
     * @return true 表示拥有权限
     */
    public static boolean hasPermission(String permission) {
        Authentication auth = currentAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
            // 检查 OchGrantedAuthority 内部的权限列表
            if (authority instanceof OchGrantedAuthority ochAuth) {
                if (ochAuth.getPermissions() != null && ochAuth.getPermissions().contains(permission)) {
                    return true;
                }
            }
        }
        return false;
    }
}
