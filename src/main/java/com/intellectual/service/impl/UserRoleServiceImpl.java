package com.intellectual.service.impl;

import com.intellectual.service.UserRoleService;
import com.intellectual.model.entity.UserRole;
import com.intellectual.mapper.UserRoleMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用户和角色关联表 N-1 服务实现类
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

}
