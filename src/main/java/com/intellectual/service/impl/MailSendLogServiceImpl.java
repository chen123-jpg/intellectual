package com.intellectual.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.intellectual.mapper.MailSendAttachmentMapper;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.MailSendAttachment;
import com.intellectual.service.MailSendLogService;
import com.intellectual.model.entity.MailSendLog;
import com.intellectual.mapper.MailSendLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件发送记录表 服务实现类
 *
 * @author 陈创
 * @since 2026-07-23 19:09
 */
@Service
public class MailSendLogServiceImpl extends ServiceImpl<MailSendLogMapper, MailSendLog> implements MailSendLogService {

    private final MailSendLogMapper mailSendLogMapper;
    private final MailSendAttachmentMapper mailSendAttachmentMapper;

    public MailSendLogServiceImpl(MailSendLogMapper mailSendLogMapper, MailSendAttachmentMapper mailSendAttachmentMapper) {
        this.mailSendLogMapper = mailSendLogMapper;
        this.mailSendAttachmentMapper = mailSendAttachmentMapper;
    }

    @Override
    public Result getlogById(Long disclosureId) {
        var query = Wrappers.lambdaQuery(MailSendLog.class)
                .orderByDesc(MailSendLog::getCreateTime);
        if (disclosureId != null) {
            query.eq(MailSendLog::getDisclosureId, disclosureId);
        }
        List<MailSendLog> logList = mailSendLogMapper.selectList(query);
        if (logList.isEmpty()) {
            return Result.fail("记录不存在");
        }

        List<Map<String, Object>> resultList = new java.util.ArrayList<>();
        for (MailSendLog mailSendLog : logList) {
            List<MailSendAttachment> attachmentList = mailSendAttachmentMapper.selectList(
                    Wrappers.lambdaQuery(MailSendAttachment.class)
                            .eq(MailSendAttachment::getMailSendLogId, mailSendLog.getId())
            );
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("mailSendLog", mailSendLog);
            dataMap.put("attachmentList", attachmentList);
            resultList.add(dataMap);
        }

        return Result.success(resultList);
    }
}
