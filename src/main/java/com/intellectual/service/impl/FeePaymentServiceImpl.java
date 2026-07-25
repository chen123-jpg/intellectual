package com.intellectual.service.impl;

import com.intellectual.service.FeePaymentService;
import com.intellectual.model.entity.FeePayment;
import com.intellectual.mapper.FeePaymentMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 缴费表 服务实现类
 *
 * @author 陈创
 * @since 2026-07-26 00:42
 */
@Service
public class FeePaymentServiceImpl extends ServiceImpl<FeePaymentMapper, FeePayment> implements FeePaymentService {

}
