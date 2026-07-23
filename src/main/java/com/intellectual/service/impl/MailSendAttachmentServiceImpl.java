package com.intellectual.service.impl;

import com.intellectual.service.MailSendAttachmentService;
import com.intellectual.model.entity.MailSendAttachment;
import com.intellectual.mapper.MailSendAttachmentMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 邮件发送附件表 服务实现类
 *
 * @author 陈创
 * @since 2026-07-23 19:09
 */
@Service
public class MailSendAttachmentServiceImpl extends ServiceImpl<MailSendAttachmentMapper, MailSendAttachment> implements MailSendAttachmentService {

}
