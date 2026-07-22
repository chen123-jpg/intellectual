package com.intellectual.service.impl;

import com.intellectual.service.RoleMenuService;
import com.intellectual.model.entity.RoleMenu;
import com.intellectual.mapper.RoleMenuMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 角色和菜单关联表 1-N 服务实现类
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenu> implements RoleMenuService {

}
