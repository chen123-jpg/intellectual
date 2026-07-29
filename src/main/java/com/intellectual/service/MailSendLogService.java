package com.intellectual.service;

import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.MailSendLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 邮件发送记录表 服务类接口
 *
 * @author 陈创
 * @since 2026-07-23 19:09
 */
public interface MailSendLogService extends IService<MailSendLog> {

    Result getlogById(Long disclosureId);
}
