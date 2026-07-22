package com.intellectual.service;

import com.intellectual.model.dto.LoginDto;
import com.intellectual.model.dto.RegisterDto;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户信息表 服务类接口
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
public interface UserService extends IService<User> {

    Result register(RegisterDto registerDto);
    Result login(LoginDto loginDto);
}
