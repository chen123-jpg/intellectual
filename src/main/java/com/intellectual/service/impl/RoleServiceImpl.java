package com.intellectual.service.impl;

import com.intellectual.service.RoleService;
import com.intellectual.model.entity.Role;
import com.intellectual.mapper.RoleMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 角色信息表 服务实现类
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

}
