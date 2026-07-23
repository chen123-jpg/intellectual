package com.intellectual.service.impl;

import com.intellectual.service.InvoiceService;
import com.intellectual.model.entity.Invoice;
import com.intellectual.mapper.InvoiceMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 开票表 服务实现类
 *
 * @author 陈创
 * @since 2026-07-23 16:59
 */
@Service
public class InvoiceServiceImpl extends ServiceImpl<InvoiceMapper, Invoice> implements InvoiceService {

}
