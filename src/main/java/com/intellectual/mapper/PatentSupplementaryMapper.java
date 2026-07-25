package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.PatentSupplementary;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 补漏专利表 Mapper
*
* @author 陈创
* @since 2026-07-25 18:08
*/
@Mapper
public interface PatentSupplementaryMapper extends BaseMapper<PatentSupplementary> {
}
