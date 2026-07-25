package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.Applicant;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 申请人表 Mapper
*
* @author 陈创
* @since 2026-07-26 00:42
*/
@Mapper
public interface ApplicantMapper extends BaseMapper<Applicant> {
}
