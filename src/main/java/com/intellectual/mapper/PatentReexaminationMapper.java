package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.PatentReexamination;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 复审无效专利表 Mapper
*
* @author 陈创
* @since 2026-07-25 18:12
*/
@Mapper
public interface PatentReexaminationMapper extends BaseMapper<PatentReexamination> {
}
