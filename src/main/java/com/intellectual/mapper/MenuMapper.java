package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.Menu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 菜单权限表 Mapper
*
* @author 陈创
* @since 2026-07-21 17:19
*/
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {
}
