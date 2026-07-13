package com.och.security.permission;

import com.och.security.authority.OchGrantedAuthority;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 权限上下文测试
 * <p>
 * 验证新权限注解和旧 Spring Security 权限体系共用同一份认证上下文，
 * 后续 Controller 不需要重复解析当前用户权限。
 * </p>
 *
 * @author danmo
 */
class PermissionContextTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void hasPermissionUsesOchGrantedAuthorityPermissions() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "agent", "N/A", List.of(new OchGrantedAuthority("ROLE_AGENT", List.of("call:skill:add")))));

        assertTrue(PermissionContext.hasPermission("call:skill:add"));
        assertFalse(PermissionContext.hasPermission("call:skill:delete"));
    }

    @Test
    void adminRoleHasAllPermissions() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", "N/A", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        assertTrue(PermissionContext.hasPermission("any:permission"));
    }

    @Test
    void unauthenticatedContextHasNoPermission() {
        assertFalse(PermissionContext.hasPermission("call:skill:add"));
    }
}
