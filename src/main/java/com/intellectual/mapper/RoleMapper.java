package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 角色信息表 Mapper
*
* @author 陈创
* @since 2026-07-21 17:19
*/
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
