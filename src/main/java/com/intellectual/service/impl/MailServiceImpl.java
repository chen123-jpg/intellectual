// src/main/java/com/intellectual/service/impl/MailServiceImpl.java
package com.intellectual.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intellectual.mapper.MailMapper;
import com.intellectual.model.entity.Mail;
import com.intellectual.model.entity.MailSendAttachment;
import com.intellectual.model.entity.MailSendLog;
import com.intellectual.model.enums.MailServerConfig;
import com.intellectual.security.LoginUser;
import com.intellectual.service.MailSendAttachmentService;
import com.intellectual.service.MailSendLogService;
import com.intellectual.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl extends ServiceImpl<MailMapper, Mail> implements MailService {

    private final MailSendLogService mailSendLogService;
    private final MailSendAttachmentService mailSendAttachmentService;

    @Override
    public MailSendLog sendMail(LoginUser sender, String to, String cc, String subject, String text,
                                List<MultipartFile> attachments) throws Exception {
        return sendMailInternal(sender, to, cc, subject, text, attachments, null);
    }

    @Override
    public MailSendLog sendMailWithFiles(LoginUser sender, String to, String cc, String subject, String text,
                                         List<Path> filePaths, List<String> fileNames) throws Exception {
        List<NamedPath> namedPaths = new ArrayList<>();
        if (filePaths != null) {
            for (int i = 0; i < filePaths.size(); i++) {
                Path path = filePaths.get(i);
                String name = (fileNames != null && i < fileNames.size() && fileNames.get(i) != null)
                        ? fileNames.get(i)
                        : path.getFileName().toString();
                namedPaths.add(new NamedPath(path, name));
            }
        }
        return sendMailInternal(sender, to, cc, subject, text, null, namedPaths);
    }

    private MailSendLog sendMailInternal(LoginUser sender, String to, String cc, String subject, String text,
                                         List<MultipartFile> multipartFiles,
                                         List<NamedPath> pathFiles) throws MessagingException, IOException {

        String email = sender.getEmail();
        String authCode = sender.getAuthCode();
        String smtpHost = sender.getSmtpHost();
        Integer smtpPort = sender.getSmtpPort();

        // 1. 先创建发送记录（PENDING）
        MailSendLog sendLog = new MailSendLog();
        sendLog.setFromEmail(email);
        sendLog.setToEmails(to);
        sendLog.setCcEmails(cc);
        sendLog.setSubject(subject);
        sendLog.setContent(text);
        sendLog.setSendStatus("PENDING");
        sendLog.setSenderUserId(sender.getUserId());
        sendLog.setSenderName(sender.getLoginName());
        sendLog.setCreateTime(new Date());
        mailSendLogService.save(sendLog);

        // 收集附件信息用于后续记录
        List<AttachmentInfo> attachmentInfos = new ArrayList<>();

        try {
            // 2. 配置 JavaMailSender
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            if (smtpHost != null && !smtpHost.isBlank() && smtpPort != null && smtpPort > 0) {
                mailSender.setHost(smtpHost);
                mailSender.setPort(smtpPort);
            } else {
                MailServerConfig config = MailServerConfig.fromEmail(email);
                if (config == null) {
                    throw new IllegalArgumentException("暂不支持该邮箱服务商: " + email + "，请在注册时填写自定义SMTP信息");
                }
                mailSender.setHost(config.getHost());
                mailSender.setPort(config.getPort());
            }
            mailSender.setUsername(email);
            mailSender.setPassword(authCode);

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            int port = mailSender.getPort();
            if (port == 587) {
                props.put("mail.smtp.starttls.enable", "true");
            } else if (port == 465) {
                props.put("mail.smtp.ssl.enable", "true");
            }
            props.put("mail.smtp.connectiontimeout", 5000);
            props.put("mail.smtp.timeout", 5000);
            props.put("mail.smtp.writetimeout", 5000);
            mailSender.setJavaMailProperties(props);

            // 3. 构建 MimeMessage
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(email);
            helper.setTo(splitEmails(to));
            if (cc != null && !cc.isBlank()) {
                helper.setCc(splitEmails(cc));
            }
            helper.setSubject(subject);
            helper.setText(text, false);

            // 4. 处理附件（MultipartFile）
            if (multipartFiles != null) {
                for (MultipartFile file : multipartFiles) {
                    if (file == null || file.isEmpty()) continue;
                    helper.addAttachment(file.getOriginalFilename(),
                            new ByteArrayResource(file.getBytes()), file.getContentType());
                    attachmentInfos.add(new AttachmentInfo(
                            file.getOriginalFilename(), null, null, file.getSize()));
                }
            }

            // 5. 处理磁盘附件
            if (pathFiles != null) {
                for (NamedPath np : pathFiles) {
                    if (np.path() == null || !np.path().toFile().exists()) continue;
                    helper.addAttachment(np.name(), new FileSystemResource(np.path().toFile()));
                    attachmentInfos.add(new AttachmentInfo(
                            np.name(), np.path().toString(), null, np.path().toFile().length()));
                }
            }

            // 6. 发送
            mailSender.send(message);
            log.info("邮件已从 {} 发送至 {}", email, to);

            // 7. 更新发送记录为成功
            sendLog.setSendStatus("SUCCESS");
            sendLog.setSentAt(new Date());
            mailSendLogService.updateById(sendLog);

            // 8. 保存附件记录
            for (AttachmentInfo info : attachmentInfos) {
                MailSendAttachment attachment = new MailSendAttachment();
                attachment.setMailSendLogId(sendLog.getId());
                attachment.setFileName(info.name);
                attachment.setFilePath(info.path);
                attachment.setFileUrl(info.url);
                attachment.setFileSize(info.size);
                attachment.setCreateTime(new Date());
                mailSendAttachmentService.save(attachment);
            }

        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());
            sendLog.setSendStatus("FAILED");
            sendLog.setErrorMessage(e.getMessage());
            mailSendLogService.updateById(sendLog);
            throw e;
        }

        return sendLog;
    }

    private String[] splitEmails(String emails) {
        return java.util.Arrays.stream(emails.split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    private record NamedPath(Path path, String name) {}

    private record AttachmentInfo(String name, String path, String url, long size) {}
}