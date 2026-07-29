package com.intellectual.service;

import com.intellectual.model.dto.LoginDto;
import com.intellectual.model.dto.LoginResult;
import com.intellectual.model.dto.RegisterDto;
import com.intellectual.model.dto.UserSaveDto;
import com.intellectual.model.vo.UserVo;
import com.intellectual.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户信息表 服务类接口
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * <p>校验账号唯一性，BCrypt 加密密码，同时创建用户与邮箱配置记录</p>
     *
     * @param registerDto 注册表单数据
     * @throws com.intellectual.exception.BusinessException 账号已存在时抛出
     */
    void register(RegisterDto registerDto);

    /**
     * 用户登录
     * <p>校验密码、账号状态，查询用户的角色与权限，生成 JWT Token 并缓存至 Redis</p>
     *
     * @param loginDto 登录表单数据
     * @return 包含 Token、用户信息、角色与权限列表的登录结果
     * @throws com.intellectual.exception.BusinessException 账号不存在、密码错误或账号停用时抛出
     */
    LoginResult login(LoginDto loginDto);

    void saveAuthCode(Long userId, String email, String authCode);

    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 查询用户详情（含角色ID列表）
     */
    UserVo getUserById(Long userId);

    /**
     * 新增用户（含角色绑定）
     */
    void createUser(UserSaveDto dto);

    /**
     * 修改用户（含角色更新）
     */
    void updateUser(UserSaveDto dto);

    /**
     * 删除用户（软删除，delFlag=2）
     */
    void deleteUser(Long userId);
}
