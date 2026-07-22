package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 用户和角色关联表 N-1 Mapper
*
* @author 陈创
* @since 2026-07-21 17:19
*/
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}
