package com.intellectual.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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

    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
