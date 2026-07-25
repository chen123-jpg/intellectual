package com.intellectual.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.intellectual.model.entity.FeePayment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* 缴费表 Mapper
*
* @author 陈创
* @since 2026-07-26 00:42
*/
@Mapper
public interface FeePaymentMapper extends BaseMapper<FeePayment> {
}
