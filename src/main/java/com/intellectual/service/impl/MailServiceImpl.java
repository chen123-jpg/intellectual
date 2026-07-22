package com.intellectual.service.impl;

import com.intellectual.service.MailService;
import com.intellectual.model.entity.Mail;
import com.intellectual.mapper.MailMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用户邮箱表 服务实现类
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
@Service
public class MailServiceImpl extends ServiceImpl<MailMapper, Mail> implements MailService {

}
