package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.PatentIntermediateChange;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 中间著变专利表（有重复） Mapper
*
* @author 陈创
* @since 2026-07-23 16:59
*/
@Mapper
public interface PatentIntermediateChangeMapper extends BaseMapper<PatentIntermediateChange> {
}
