package com.och.security.authority;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author danmo
 * @date 2024-02-23 10:44
 **/
@Component("authz")
public class OchMenuAuthorization {

    /**
     * 检查当前用户是否拥有指定权限
     * <p>遍历用户的所有角色，任一角色拥有指定权限或为 ROLE_ADMIN（超级管理员）
     * 则返回 true。修复了之前只检查第一个角色的逻辑错误。</p>
     *
     * @param permission 权限标识（如 call:skill:delete）
     * @return true 拥有权限，false 无权限
     */
    public boolean hasPerm(String permission) {
        List<OchGrantedAuthority> authorities = (List<OchGrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        for (OchGrantedAuthority authority : authorities) {
            // 超级管理员拥有所有权限，直接通过
            if (StringUtils.equals(authority.getAuthority(), "ROLE_ADMIN")) {
                return true;
            }
            // 检查当前角色是否包含目标权限
            if (authority.getPermissions().contains(permission)) {
                return true;
            }
        }
        return false;
    }
}
