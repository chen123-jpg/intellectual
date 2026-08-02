package com.intellectual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellectual.mapper.MailMapper;
import com.intellectual.mapper.MailSendAttachmentMapper;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.Mail;
import com.intellectual.model.entity.MailSendAttachment;
import com.intellectual.model.entity.MailSendLog;
import com.intellectual.model.enums.MailSendStatus;
import com.intellectual.service.MailSendLogService;
import com.intellectual.mapper.MailSendLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
    private final MailMapper mailMapper;

    public MailSendLogServiceImpl(MailSendLogMapper mailSendLogMapper,
                                  MailSendAttachmentMapper mailSendAttachmentMapper,
                                  MailMapper mailMapper) {
        this.mailSendLogMapper = mailSendLogMapper;
        this.mailSendAttachmentMapper = mailSendAttachmentMapper;
        this.mailMapper = mailMapper;
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

    @Override
    public Result getPage(Long userId, Integer current, Integer size) {
        var query = Wrappers.lambdaQuery(MailSendLog.class)
                .eq(userId != null, MailSendLog::getSenderUserId, userId)
                .orderByDesc(MailSendLog::getCreateTime);
        Page<MailSendLog> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        Page<MailSendLog> result = mailSendLogMapper.selectPage(page, query);
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        return Result.success(data);
    }

    @Override
    public Result getById(Long id) {
        MailSendLog log = mailSendLogMapper.selectById(id);
        if (log == null) {
            return Result.fail("记录不存在");
        }
        List<MailSendAttachment> attachmentList = mailSendAttachmentMapper.selectList(
                Wrappers.lambdaQuery(MailSendAttachment.class)
                        .eq(MailSendAttachment::getMailSendLogId, id)
        );
        Map<String, Object> data = new HashMap<>();
        data.put("mailSendLog", log);
        data.put("attachmentList", attachmentList);
        return Result.success(data);
    }

    @Override
    public Result deleteById(Long id) {
        MailSendLog log = mailSendLogMapper.selectById(id);
        if (log == null) {
            return Result.fail("记录不存在");
        }
        mailSendLogMapper.deleteById(id);
        mailSendAttachmentMapper.delete(
                Wrappers.lambdaQuery(MailSendAttachment.class)
                        .eq(MailSendAttachment::getMailSendLogId, id)
        );
        return Result.successMsg("删除成功");
    }

    @Override
    public Result batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail("请选择要删除的记录");
        }
        mailSendLogMapper.deleteByIds(ids);
        mailSendAttachmentMapper.delete(
                Wrappers.lambdaQuery(MailSendAttachment.class)
                        .in(MailSendAttachment::getMailSendLogId, ids)
        );
        return Result.successMsg("批量删除成功");
    }

    @Override
    public Result resend(Long id) {
        MailSendLog log = mailSendLogMapper.selectById(id);
        if (log == null) {
            return Result.fail("记录不存在");
        }
        if (log.getSendStatus() == null || log.getSendStatus() != MailSendStatus.FAILED.getCode()) {
            return Result.fail("仅失败记录支持重新发送");
        }
        if (log.getSenderUserId() == null) {
            return Result.fail("发送人信息缺失，无法重新发送");
        }

        Mail mail = mailMapper.selectOne(new LambdaQueryWrapper<Mail>()
                .eq(Mail::getUserId, log.getSenderUserId()));
        if (mail == null) {
            return Result.fail("发送人邮箱配置不存在");
        }

        try {
            JavaMailSenderImpl mailSender = createMailSender(
                    mail.getSmtpHost(), mail.getSmtpPort(),
                    mail.getEmail(), mail.getAuthCode());

            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(log.getFromEmail());
            helper.setTo(log.getToEmails().split("[,;]"));
            if (log.getCcEmails() != null && !log.getCcEmails().isBlank()) {
                helper.setCc(log.getCcEmails().split("[,;]"));
            }
            helper.setSubject(log.getSubject());
            helper.setText(log.getContent(), true);

            mailSender.send(message);

            log.setSendStatus(MailSendStatus.SUCCESS.getCode());
            log.setSentAt(new Date());
            log.setErrorMessage(null);
            mailSendLogMapper.updateById(log);
            return Result.successMsg("重新发送成功");
        } catch (Exception e) {
            log.setSendStatus(MailSendStatus.FAILED.getCode());
            log.setErrorMessage(e.getMessage());
            mailSendLogMapper.updateById(log);
            return Result.fail("重新发送失败：" + e.getMessage());
        }
    }

    private JavaMailSenderImpl createMailSender(String host, Integer port, String username, String password) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port != null ? port : 587);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        if (mailSender.getPort() == 587) {
            props.put("mail.smtp.starttls.enable", "true");
        } else if (mailSender.getPort() == 465) {
            props.put("mail.smtp.ssl.enable", "true");
        }
        props.put("mail.smtp.connectiontimeout", 10000);
        props.put("mail.smtp.timeout", 10000);
        props.put("mail.smtp.writetimeout", 10000);
        mailSender.setJavaMailProperties(props);
        return mailSender;
    }
}
