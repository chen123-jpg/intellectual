package com.intellectual.service;

import com.intellectual.model.entity.MailTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 邮件模板表 服务类接口
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
public interface MailTemplateService extends IService<MailTemplate> {

    MailTemplate getByCode(String templateCode);

    List<MailTemplate> listEnabled();
}
