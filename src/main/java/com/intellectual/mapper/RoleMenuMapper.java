package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.RoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 角色和菜单关联表 1-N Mapper
*
* @author 陈创
* @since 2026-07-21 17:19
*/
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {
}
