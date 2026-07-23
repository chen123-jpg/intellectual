package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.PatentDisclosure;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 专利交底信息表（T表） Mapper
*
* @author 陈创
* @since 2026-07-23 16:59
*/
@Mapper
public interface PatentDisclosureMapper extends BaseMapper<PatentDisclosure> {
}
