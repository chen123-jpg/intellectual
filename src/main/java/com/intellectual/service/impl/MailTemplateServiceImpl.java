package com.intellectual.service.impl;

import com.intellectual.service.MailTemplateService;
import com.intellectual.model.entity.MailTemplate;
import com.intellectual.mapper.MailTemplateMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 邮件模板表 服务实现类
 *
 * @author 陈创
 * @since 2026-07-23 19:09
 */
@Service
public class MailTemplateServiceImpl extends ServiceImpl<MailTemplateMapper, MailTemplate> implements MailTemplateService {

}
