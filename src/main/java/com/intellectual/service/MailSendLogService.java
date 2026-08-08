package com.intellectual.service;

import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.MailSendLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 邮件发送记录表 服务类接口
 *
 * @author 陈创
 * @since 2026-07-23 19:09
 */
public interface MailSendLogService extends IService<MailSendLog> {

    Result getLogByReferenceId(String referenceId);

    Result getPage(Long userId, Integer current, Integer size);

    Result getById(Long id);

    Result deleteById(Long id);

    Result batchDelete(List<Long> ids);

    Result resend(Long id);
}
