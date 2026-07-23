package com.intellectual.service.impl;

import com.intellectual.service.MailSendLogService;
import com.intellectual.model.entity.MailSendLog;
import com.intellectual.mapper.MailSendLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 邮件发送记录表 服务实现类
 *
 * @author 陈创
 * @since 2026-07-23 19:09
 */
@Service
public class MailSendLogServiceImpl extends ServiceImpl<MailSendLogMapper, MailSendLog> implements MailSendLogService {

}
