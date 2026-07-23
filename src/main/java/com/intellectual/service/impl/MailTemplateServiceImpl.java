package com.intellectual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellectual.service.MailTemplateService;
import com.intellectual.model.entity.MailTemplate;
import com.intellectual.mapper.MailTemplateMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 邮件模板表 服务实现类
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
@Service
public class MailTemplateServiceImpl extends ServiceImpl<MailTemplateMapper, MailTemplate> implements MailTemplateService {

    @Override
    public MailTemplate getByCode(String templateCode) {
        return getOne(new LambdaQueryWrapper<MailTemplate>()
                .eq(MailTemplate::getTemplateCode, templateCode));
    }

    @Override
    public List<MailTemplate> listEnabled() {
        return list(new LambdaQueryWrapper<MailTemplate>()
                .eq(MailTemplate::getEnabled, 1)
                .orderByDesc(MailTemplate::getCreateTime));
    }
}