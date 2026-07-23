package com.intellectual.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.intellectual.exception.BusinessException;
import com.intellectual.mapper.MailMapper;
import com.intellectual.mapper.RoleMenuMapper;
import com.intellectual.mapper.UserRoleMapper;
import com.intellectual.model.constants.Constants;
import com.intellectual.model.dto.LoginDto;
import com.intellectual.model.dto.LoginResult;
import com.intellectual.model.dto.RegisterDto;
import com.intellectual.model.entity.Mail;
import com.intellectual.model.entity.Menu;
import com.intellectual.model.entity.Role;
import com.intellectual.model.entity.RoleMenu;
import com.intellectual.model.entity.User;
import com.intellectual.model.entity.UserRole;
import com.intellectual.mapper.MenuMapper;
import com.intellectual.mapper.RoleMapper;
import com.intellectual.mapper.UserMapper;
import com.intellectual.redis.RedisUtils;
import com.intellectual.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intellectual.utils.ArrayUtils;
import com.intellectual.utils.JwtUtils;
import com.intellectual.utils.PasswordUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户信息表 服务实现类
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordUtils passwordUtils;
    private final MailMapper mailMapper;
    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;

    public UserServiceImpl(UserMapper userMapper, PasswordUtils passwordUtils,
                           MailMapper mailMapper, JwtUtils jwtUtils, RedisUtils redisUtils,
                           UserRoleMapper userRoleMapper, RoleMapper roleMapper,
                           RoleMenuMapper roleMenuMapper, MenuMapper menuMapper) {
        this.userMapper = userMapper;
        this.passwordUtils = passwordUtils;
        this.mailMapper = mailMapper;
        this.jwtUtils = jwtUtils;
        this.redisUtils = redisUtils;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuMapper = menuMapper;
    }

    /**
     * 用户注册
     * <p>在一个事务中完成：校验账号唯一 → BCrypt 加密密码 → 插入用户记录 → 创建邮箱配置</p>
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void register(RegisterDto registerDto) {
        // 检查登录名是否已存在
        User oldUser = userMapper.selectOne(
                Wrappers.lambdaQuery(User.class).eq(User::getLoginName, registerDto.getLoginName()));
        if (oldUser != null) {
            throw new BusinessException("账号已存在");
        }
        // BCrypt 加密密码后保存用户
        registerDto.setPassword(PasswordUtils.encode(registerDto.getPassword()));
        User user = ArrayUtils.copyProperties(registerDto, new User());
        user.setStatus("0");       // 默认正常状态
        user.setDelFlag("0");      // 未删除
        user.setUserType("01");    // 注册用户
        userMapper.insert(user);
        // 同步创建用户邮箱配置
        Mail mail = new Mail();
        mail.setEmail(user.getEmail());
        mailMapper.insert(mail);
    }

    /**
     * 用户登录
     *
     * <p>完整登录流程：</p>
     * <ol>
     *   <li>根据登录名查询用户，不存在则抛出异常</li>
     *   <li>BCrypt 比对密码，不匹配则抛出异常</li>
     *   <li>检查账号状态（status=0 为正常）</li>
     *   <li>查询用户关联的角色和权限标识</li>
     *   <li>生成 JWT Token 并缓存至 Redis（1 天有效期）</li>
     *   <li>组装 LoginResult 返回</li>
     * </ol>
     */
    @Override
    public LoginResult login(LoginDto loginDto) {
        // 1. 查询用户
        User user = userMapper.selectOne(
                Wrappers.lambdaQuery(User.class).eq(User::getLoginName, loginDto.getLoginName()));
        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }
        // 2. 校验密码（统一返回"账号或密码错误"，避免信息泄露）
        if (!PasswordUtils.matches(loginDto.getPassword(), user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        // 3. 检查账号状态
        if (!"0".equals(user.getStatus())) {
            throw new BusinessException("账号已被停用");
        }

        // 4. 查询角色与权限
        Set<String> roleKeys = Set.of();
        Set<String> permissions = Set.of();

        List<UserRole> userRoles = userRoleMapper.selectList(
                Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getUserId, user.getUserId()));
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();

        if (!roleIds.isEmpty()) {
            // 查询角色标识
            List<Role> roles = roleMapper.selectBatchIds(roleIds);
            roleKeys = roles.stream()
                    .map(Role::getRoleKey)
                    .filter(k -> k != null && !k.isEmpty())
                    .collect(Collectors.toSet());

            // 查询角色关联的菜单权限
            List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                    Wrappers.lambdaQuery(RoleMenu.class).in(RoleMenu::getRoleId, roleIds));
            List<Long> menuIds = roleMenus.stream().map(RoleMenu::getMenuId).distinct().toList();

            if (!menuIds.isEmpty()) {
                List<Menu> menus = menuMapper.selectBatchIds(menuIds);
                permissions = menus.stream()
                        .map(Menu::getPerms)
                        .filter(p -> p != null && !p.isEmpty())
                        .collect(Collectors.toSet());
            }
        }

        // 5. 生成 JWT 并缓存至 Redis
        String token = jwtUtils.generateToken(user.getUserId(), user.getLoginName());
        redisUtils.set(Constants.REDIS_KEY_TOKEN + user.getUserId(), token, Constants.REDIS_TIME_1DAY);

        // 6. 组装响应
        return LoginResult.builder()
                .token(token)
                .userId(user.getUserId())
                .loginName(user.getLoginName())
                .userName(user.getUserName())
                .roles(roleKeys)
                .permissions(permissions)
                .email(user.getEmail())
                .build();
    }
}
