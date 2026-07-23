package com.intellectual.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 登录成功响应体
 * <p>包含 JWT Token、用户基本信息、角色与权限列表，前端据此控制菜单与按钮显隐</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResult {

    /** JWT 访问令牌 */
    private String token;

    /** 用户 ID */
    private Long userId;

    /** 登录账号 */
    private String loginName;

    /** 用户昵称 */
    private String userName;

    /** 角色标识集合 */
    private Set<String> roles;

    /** 权限标识集合 */
    private Set<String> permissions;

    private String email;
}
