package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.DisclosureStatusLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 交底状态变更日志 Mapper
*
* @author 陈创
* @since 2026-07-26 00:42
*/
@Mapper
public interface DisclosureStatusLogMapper extends BaseMapper<DisclosureStatusLog> {
}
