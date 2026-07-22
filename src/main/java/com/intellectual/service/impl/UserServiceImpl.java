package com.intellectual.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.intellectual.mapper.MailMapper;
import com.intellectual.model.dto.LoginDto;
import com.intellectual.model.dto.RegisterDto;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.Mail;
import com.intellectual.service.UserService;
import com.intellectual.model.entity.User;
import com.intellectual.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intellectual.utils.ArrayUtils;
import com.intellectual.utils.PasswordUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Wrapper;

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

    public UserServiceImpl(UserMapper userMapper, PasswordUtils passwordUtils, MailMapper mailMapper) {
        this.userMapper = userMapper;
        this.passwordUtils = passwordUtils;
        this.mailMapper = mailMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result register(RegisterDto registerDto) {
        User oldUser = userMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getLoginName,registerDto.getLoginName()));
        if(oldUser != null){
            return Result.fail("账号已存在");
        }
        registerDto.setPassword(PasswordUtils.encode(registerDto.getPassword()));
        User user = ArrayUtils.copyProperties(registerDto,new User());
        userMapper.insert(user);
        Mail mail = new Mail();
        mail.setEmail(user.getEmail());
        mailMapper.insert(mail);
        return  Result.success(null);
    }

    @Override
    public Result login(LoginDto loginDto) {

    }
}
