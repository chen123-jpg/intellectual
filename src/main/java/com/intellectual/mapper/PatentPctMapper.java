package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.PatentPct;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* PCT国际申请表 Mapper
*
* @author 陈创
* @since 2026-07-25 18:12
*/
@Mapper
public interface PatentPctMapper extends BaseMapper<PatentPct> {
}
