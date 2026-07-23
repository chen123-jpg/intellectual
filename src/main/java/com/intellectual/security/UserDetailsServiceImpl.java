package com.intellectual.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.intellectual.mapper.*;
import com.intellectual.model.entity.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security 用户详情加载实现
 * <p>负责从数据库加载用户信息及其关联的角色与权限，组装为 {@link LoginUser}。
 * 支持按登录名加载（标准 {@link UserDetailsService} 接口）和按用户 ID 加载（JWT 场景）两种模式。</p>
 *
 * <p>数据加载链路：</p>
 * <pre>
 * sys_user ──&gt; sys_user_role ──&gt; sys_role ──&gt; sys_role_menu ──&gt; sys_menu.perms
 *                (用户角色)        (角色)        (角色菜单)          (权限标识)
 * </pre>
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;

    private final MailMapper mailMapper;

    public UserDetailsServiceImpl(UserMapper userMapper, UserRoleMapper userRoleMapper,
                                  RoleMapper roleMapper, RoleMenuMapper roleMenuMapper,
                                  MenuMapper menuMapper, MailMapper mailMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuMapper = menuMapper;
        this.mailMapper = mailMapper;
    }

    /**
     * 按登录名加载（标准 UserDetailsService 接口，供 DaoAuthenticationProvider 使用）
     */
    @Override
    public UserDetails loadUserByUsername(String loginName) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                Wrappers.lambdaQuery(User.class).eq(User::getLoginName, loginName));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + loginName);
        }
        return buildLoginUser(user);
    }

    /**
     * 按用户 ID 加载（JWT 场景，每次请求从 Token 中解析 userId 后调用）
     */
    public LoginUser loadUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + userId);
        }
        return buildLoginUser(user);
    }

    /**
     * 根据用户实体组装完整的 LoginUser
     * <p>查询链路：用户 → 用户角色关联 → 角色信息 → 角色菜单关联 → 权限标识</p>
     */
    private LoginUser buildLoginUser(User user) {
        // 1. 查询用户拥有的角色 ID 列表
        List<UserRole> userRoles = userRoleMapper.selectList(
                Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getUserId, user.getUserId()));
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();

        Set<String> roleKeys = Set.of();
        Set<String> permissions = Set.of();

        if (!roleIds.isEmpty()) {
            // 2. 查询角色实体，提取角色标识 role_key
            List<Role> roles = roleMapper.selectBatchIds(roleIds);
            roleKeys = roles.stream()
                    .map(Role::getRoleKey)
                    .filter(k -> k != null && !k.isEmpty())
                    .collect(Collectors.toSet());

            // 3. 查询角色-菜单关联表，获取该角色拥有的菜单 ID
            List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                    Wrappers.lambdaQuery(RoleMenu.class).in(RoleMenu::getRoleId, roleIds));
            List<Long> menuIds = roleMenus.stream().map(RoleMenu::getMenuId).distinct().toList();

            if (!menuIds.isEmpty()) {
                // 4. 查询菜单实体，提取权限标识 perms
                List<Menu> menus = menuMapper.selectBatchIds(menuIds);
                permissions = menus.stream()
                        .map(Menu::getPerms)
                        .filter(p -> p != null && !p.isEmpty())
                        .collect(Collectors.toSet());
            }
        }
        //根据loginName 查询 Mail 信息
        Mail mail = mailMapper.selectOne(
                Wrappers.lambdaQuery(Mail.class).eq(Mail::getUserId, user.getUserId())
        );

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setLoginName(user.getLoginName());
        loginUser.setPassword(user.getPassword());
        loginUser.setStatus(user.getStatus());
        loginUser.setRoles(roleKeys);
        loginUser.setPermissions(permissions);
        if (mail != null){
            loginUser.setEmail(mail.getEmail());
            loginUser.setAuthCode(mail.getAuthCode());
            loginUser.setSmtpHost(mail.getSmtpHost());
            loginUser.setSmtpPort(mail.getSmtpPort());
        }
        return loginUser;
    }
}
