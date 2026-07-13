package com.och.security.authority;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

import java.util.List;

/**
 * 自定义权限封装
 * <p>实现 Spring Security 的 GrantedAuthority 接口，除角色标识外还携带具体的权限列表。
 * 与 LoginUserInfo 配合使用，支持细粒度的功能权限校验。</p>
 *
 * @author danmo
 * @date 2024-02-23 10:49
 */
@Data
public final class OchGrantedAuthority implements GrantedAuthority {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;


    private final String role;

    private final List<String> permissions;

    public OchGrantedAuthority(String role, List<String> permissions) {
        this.role = role;
        this.permissions = permissions;
    }

    @Override
    public String getAuthority() {
        return this.role;
    }
}
