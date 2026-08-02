package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.DeadlineTypeConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 期限类型配置表 Mapper
*
* @author 陈创
* @since 2026-08-01 17:00
*/
@Mapper
public interface DeadlineTypeConfigMapper extends BaseMapper<DeadlineTypeConfig> {
}
