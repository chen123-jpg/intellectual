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
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
@Slf4j
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
    public Result getLogByReferenceId(String referenceId) {
        var query = Wrappers.lambdaQuery(MailSendLog.class)
                .orderByDesc(MailSendLog::getCreateTime);
        if (referenceId != null && !referenceId.isBlank()) {
            query.eq(MailSendLog::getReferenceId, referenceId);
        }
        List<MailSendLog> logList = mailSendLogMapper.selectList(query);

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
        MailSendLog sendLog = mailSendLogMapper.selectById(id);
        if (sendLog == null) {
            return Result.fail("记录不存在");
        }
        if (sendLog.getSendStatus() == null || sendLog.getSendStatus() != MailSendStatus.FAILED.getCode()) {
            return Result.fail("仅失败记录支持重新发送");
        }
        if (sendLog.getSenderUserId() == null) {
            return Result.fail("发送人信息缺失，无法重新发送");
        }

        Mail mail = mailMapper.selectOne(new LambdaQueryWrapper<Mail>()
                .eq(Mail::getUserId, sendLog.getSenderUserId()));
        if (mail == null) {
            return Result.fail("发送人邮箱配置不存在");
        }

        try {
            JavaMailSenderImpl mailSender = createMailSender(
                    mail.getSmtpHost(), mail.getSmtpPort(),
                    mail.getEmail(), mail.getAuthCode());

            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(sendLog.getFromEmail());
            helper.setTo(sendLog.getToEmails().split("[,;]"));
            if (sendLog.getCcEmails() != null && !sendLog.getCcEmails().isBlank()) {
                helper.setCc(sendLog.getCcEmails().split("[,;]"));
            }
            helper.setSubject(sendLog.getSubject());

            // 重新内联图片并重新挂载附件，与初次发送保持一致
            List<MailSendAttachment> attachments = mailSendAttachmentMapper.selectList(
                    Wrappers.lambdaQuery(MailSendAttachment.class)
                            .eq(MailSendAttachment::getMailSendLogId, id)
            );
            String content = sendLog.getContent();
            for (MailSendAttachment att : attachments) {
                try {
                    if (att.getFilePath() == null || att.getFileName() == null) continue;
                    Path diskPath = Paths.get(att.getFilePath()).toAbsolutePath().normalize();
                    if (!Files.exists(diskPath)) continue;
                    if (isImageFile(att.getFileName())) {
                        String cid = "img-" + System.currentTimeMillis() + "-" + att.getFileName().replaceAll("[^a-zA-Z0-9.]", "_");
                        helper.addInline(cid, new FileSystemResource(diskPath), inferMimeType(att.getFileName()));
                        if (att.getFileUrl() != null) {
                            String srcAttr = "src=\"" + att.getFileUrl() + "\"";
                            String imgTag = "<img src=\"cid:" + cid + "\" style=\"max-width:100%\" />";
                            if (content.contains(srcAttr)) {
                                content = content.replace(srcAttr, "src=\"cid:" + cid + "\"");
                            } else if (content.contains(att.getFileUrl())) {
                                content = content.replace(att.getFileUrl(), imgTag);
                            }
                        }
                    } else {
                        helper.addAttachment(att.getFileName(), new FileSystemResource(diskPath));
                    }
                } catch (Exception e) {
                    log.warn("重发添加附件失败: {}", att.getFileName(), e);
                }
            }
            helper.setText(content, true);

            mailSender.send(message);

            sendLog.setSendStatus(MailSendStatus.SUCCESS.getCode());
            sendLog.setSentAt(new Date());
            sendLog.setErrorMessage(null);
            mailSendLogMapper.updateById(sendLog);
            return Result.successMsg("重新发送成功");
        } catch (Exception e) {
            sendLog.setSendStatus(MailSendStatus.FAILED.getCode());
            sendLog.setErrorMessage(e.getMessage());
            mailSendLogMapper.updateById(sendLog);
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

    private boolean isImageFile(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".bmp")
                || lower.endsWith(".svg");
    }

    private String inferMimeType(String fileName) {
        if (fileName == null) return "application/octet-stream";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp")) return "image/bmp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "image/jpeg";
    }
}
