package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.Applicant;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 申请人表 Mapper
*
* @author 陈创
* @since 2026-07-23 16:59
*/
@Mapper
public interface ApplicantMapper extends BaseMapper<Applicant> {
}
