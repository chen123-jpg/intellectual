package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.ApplicationPackage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 申请包表(XML包与五书WORD分条目) Mapper
*
* @author 陈创
* @since 2026-07-23 16:59
*/
@Mapper
public interface ApplicationPackageMapper extends BaseMapper<ApplicationPackage> {
}
