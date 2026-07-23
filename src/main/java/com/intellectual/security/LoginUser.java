package com.intellectual.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security 登录用户主体，实现 {@link UserDetails} 接口
 * <p>封装当前登录用户的身份信息、角色集合与权限集合，
 * 在 JWT 认证成功后放入 {@code SecurityContextHolder}</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {

    /** 用户 ID */
    private Long userId;

    /** 登录账号 */
    private String loginName;

    /** 加密后的密码 */
    private String password;

    /** 账号状态（0 正常 / 1 停用） */
    private String status;

    /** 权限标识集合，来自 sys_menu.perms */
    private Set<String> permissions;

    /** 角色标识集合，来自 sys_role.role_key */
    private Set<String> roles;

    private String email;

    private String authCode;

    private String smtpHost;

    private Integer smtpPort;

    /**
     * 构建 Spring Security 权限列表
     * <p>角色会加上 {@code ROLE_} 前缀，权限直接使用原始标识</p>
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> allAuthorities = new java.util.HashSet<>();
        if (roles != null) {
            for (String role : roles) {
                allAuthorities.add("ROLE_" + role);
            }
        }
        if (permissions != null) {
            allAuthorities.addAll(permissions);
        }
        return allAuthorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return loginName;
    }

    /** 账号是否未过期（默认 true，不做过期校验） */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** 账号是否未锁定（停用状态视为锁定） */
    @Override
    public boolean isAccountNonLocked() {
        return "0".equals(status);
    }

    /** 凭证是否未过期（默认 true） */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** 账号是否启用（停用状态视为禁用） */
    @Override
    public boolean isEnabled() {
        return "0".equals(status);
    }
}
