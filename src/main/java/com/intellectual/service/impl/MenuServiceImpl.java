package com.intellectual.service.impl;

import com.intellectual.service.MenuService;
import com.intellectual.model.entity.Menu;
import com.intellectual.mapper.MenuMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 菜单权限表 服务实现类
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

}
