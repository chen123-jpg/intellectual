package com.intellectual.service.impl;

import com.intellectual.model.dto.MailRequest;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.Mail;
import com.intellectual.model.entity.MailSendAttachment;
import com.intellectual.model.entity.MailSendLog;
import com.intellectual.mapper.MailMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intellectual.model.entity.MailTemplate;
import com.intellectual.model.enums.MailSendStatus;
import com.intellectual.security.LoginUser;
import com.intellectual.service.MailSendAttachmentService;
import com.intellectual.service.MailSendLogService;
import com.intellectual.service.MailService;
import com.intellectual.service.MailTemplateService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;

/**
 * 用户邮箱表 服务实现类
 *
 * @author 陈创
 * @since 2026-07-23 19:09
 */
@Slf4j
@Service
public class MailServiceImpl extends ServiceImpl<MailMapper, Mail> implements MailService {

    @Autowired
    private MailSendAttachmentService mailSendAttachmentService;

    @Autowired
    private MailSendLogService mailSendLogService;

    @Autowired
    private UploadFileServiceImpl uploadFileServiceImpl;

    @Value("${app.upload.dir}")
    private String uploadDir;

    private Path uploadPath;
    @Autowired
    private MailTemplateService mailTemplateService;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @PostConstruct
    public void init() {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public Result sendMail(String to, String subject, String content, String cc,
                           boolean isHtml, MultipartFile file) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        String email = loginUser.getEmail();
        String authCode = loginUser.getAuthCode();
        String smtpHost = loginUser.getSmtpHost();
        Integer smtpPort = loginUser.getSmtpPort();

        if(authCode == null || smtpPort == null || smtpHost ==null){
            return Result.fail("未填写授权码，请在个人中心填写后重试");
        }

        // 1. 保存发送记录
        MailSendLog sendLog = new MailSendLog();
        sendLog.setFromEmail(email);
        sendLog.setToEmails(to);
        sendLog.setCcEmails(cc != null ? cc : "");
        sendLog.setSubject(subject);
        sendLog.setContent(content);
        sendLog.setSendStatus(MailSendStatus.PENDING.getCode());
        sendLog.setSenderUserId(loginUser.getUserId());
        sendLog.setSenderName(loginUser.getLoginName());
        sendLog.setCreateTime(new Date());
        mailSendLogService.save(sendLog);

        // 2. 上传附件并保存附件记录
        if (file != null && !file.isEmpty()) {
            try {
                Result uploadResult = uploadFileServiceImpl.upload(file);
                if (uploadResult.getCode() == 200) {
                    String fileUrl = (String) uploadResult.getData();

                    String encodedName = UriComponentsBuilder.fromUriString(fileUrl)
                            .build().getQueryParams().getFirst("name");
                    String originalName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8);

                    // 从 URL 中提取 fileId 计算磁盘路径
                    String pathPart = fileUrl.contains("?")
                            ? fileUrl.substring(0, fileUrl.indexOf("?"))
                            : fileUrl;
                    String fileId = pathPart.substring(pathPart.lastIndexOf("/") + 1);
                    Path diskPath = uploadPath.resolve(fileId).normalize();
                    long fileSize = Files.exists(diskPath) ? Files.size(diskPath) : 0;

                    MailSendAttachment attachment = new MailSendAttachment();
                    attachment.setMailSendLogId(sendLog.getId());
                    attachment.setFileName(originalName);
                    attachment.setFilePath(diskPath.toString());
                    attachment.setFileUrl(fileUrl);
                    attachment.setFileSize(fileSize);
                    attachment.setCreateTime(new Date());
                    mailSendAttachmentService.save(attachment);

                    log.info("附件已保存: name={}, diskPath={}, fileUrl={}", originalName, diskPath, fileUrl);
                }
            } catch (Exception e) {
                log.error("附件上传/保存失败", e);
            }
        }

        // 3. 发送邮件
        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(smtpHost);
            mailSender.setPort(smtpPort != null ? smtpPort : 587);
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
            props.put("mail.smtp.connectiontimeout", 10000);
            props.put("mail.smtp.timeout", 10000);
            props.put("mail.smtp.writetimeout", 10000);
            mailSender.setJavaMailProperties(props);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(email);
            helper.setTo(to.split("[,;]"));
            if (cc != null && !cc.isBlank()) {
                helper.setCc(cc.split("[,;]"));
            }
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            mailSender.send(message);
            log.info("邮件已从 {} 发送至 {}", email, to);

            sendLog.setSendStatus(MailSendStatus.SUCCESS.getCode());
            sendLog.setSentAt(new Date());
            mailSendLogService.updateById(sendLog);

            return Result.success(sendLog, "发送成功");
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());
            sendLog.setSendStatus(MailSendStatus.FAILED.getCode());
            sendLog.setErrorMessage(e.getMessage());
            mailSendLogService.updateById(sendLog);
            return Result.fail("发送失败：" + e.getMessage());
        }
    }

    @Override
    public Result sendMailWithTemplate(MailRequest request) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        String email = loginUser.getEmail();
        String authCode = loginUser.getAuthCode();
        String smtpHost = loginUser.getSmtpHost();
        Integer smtpPort = loginUser.getSmtpPort();

        if (authCode == null || smtpPort == null || smtpHost == null) {
            return Result.fail("未填写授权码，请在个人中心填写后重试");
        }

        // 1. 查找模板
        MailTemplate template = mailTemplateService.getOne(
                new LambdaQueryWrapper<MailTemplate>()
                        .eq(MailTemplate::getTemplateCode, request.getTemplateCode())
                        .eq(MailTemplate::getEnabled, 1)
        );
        if (template == null) {
            return Result.fail("模板不存在或未启用");
        }

        // 2. Thymeleaf 渲染主题和正文
        Context context = new Context();
        if (request.getTemplateData() != null) {
            context.setVariables(request.getTemplateData());
        }
        String subject = request.getSubject() != null && !request.getSubject().isBlank()
                ? request.getSubject()
                : templateEngine.process(template.getSubject(), context);
        String content = request.getText() != null && !request.getText().isBlank()
                ? request.getText()
                : templateEngine.process(template.getContent(), context);

        // 3. 保存发送记录
        MailSendLog sendLog = new MailSendLog();
        sendLog.setFromEmail(email);
        sendLog.setToEmails(request.getTo());
        sendLog.setCcEmails(request.getCc() != null ? request.getCc() : "");
        sendLog.setSubject(subject);
        sendLog.setContent(content);
        sendLog.setSendStatus(MailSendStatus.PENDING.getCode());
        sendLog.setSenderUserId(loginUser.getUserId());
        sendLog.setSenderName(loginUser.getLoginName());
        sendLog.setCreateTime(new Date());
        mailSendLogService.save(sendLog);

        // 4. 处理预上传的附件 URL，保存附件记录
        if (request.getAttachmentUrls() != null && !request.getAttachmentUrls().isEmpty()) {
            for (String fileUrl : request.getAttachmentUrls()) {
                try {
                    String encodedName = UriComponentsBuilder.fromUriString(fileUrl)
                            .build().getQueryParams().getFirst("name");
                    String originalName = encodedName != null
                            ? URLDecoder.decode(encodedName, StandardCharsets.UTF_8)
                            : fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

                    String pathPart = fileUrl.contains("?")
                            ? fileUrl.substring(0, fileUrl.indexOf("?"))
                            : fileUrl;
                    String fileId = pathPart.substring(pathPart.lastIndexOf("/") + 1);
                    Path diskPath = uploadPath.resolve(fileId).normalize();
                    long fileSize = Files.exists(diskPath) ? Files.size(diskPath) : 0;

                    MailSendAttachment attachment = new MailSendAttachment();
                    attachment.setMailSendLogId(sendLog.getId());
                    attachment.setFileName(originalName);
                    attachment.setFilePath(diskPath.toString());
                    attachment.setFileUrl(fileUrl);
                    attachment.setFileSize(fileSize);
                    attachment.setCreateTime(new Date());
                    mailSendAttachmentService.save(attachment);
                } catch (Exception e) {
                    log.error("附件记录保存失败: {}", fileUrl, e);
                }
            }
        }

        // 5. 发送邮件
        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(smtpHost);
            mailSender.setPort(smtpPort != null ? smtpPort : 587);
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
            props.put("mail.smtp.connectiontimeout", 10000);
            props.put("mail.smtp.timeout", 10000);
            props.put("mail.smtp.writetimeout", 10000);
            mailSender.setJavaMailProperties(props);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(email);
            helper.setTo(request.getTo().split("[,;]"));
            if (request.getCc() != null && !request.getCc().isBlank()) {
                helper.setCc(request.getCc().split("[,;]"));
            }
            helper.setSubject(subject);
            helper.setText(content, true);

            // 将附件添加到邮件中
            if (request.getAttachmentUrls() != null) {
                for (String fileUrl : request.getAttachmentUrls()) {
                    try {
                        String pathPart = fileUrl.contains("?")
                                ? fileUrl.substring(0, fileUrl.indexOf("?"))
                                : fileUrl;
                        String fileId = pathPart.substring(pathPart.lastIndexOf("/") + 1);
                        Path diskPath = uploadPath.resolve(fileId).normalize();
                        if (Files.exists(diskPath)) {
                            String encodedName = UriComponentsBuilder.fromUriString(fileUrl)
                                    .build().getQueryParams().getFirst("name");
                            String attachName = encodedName != null
                                    ? URLDecoder.decode(encodedName, StandardCharsets.UTF_8)
                                    : fileId;
                            helper.addAttachment(attachName, diskPath.toFile());
                        }
                    } catch (Exception e) {
                        log.error("添加附件到邮件失败: {}", fileUrl, e);
                    }
                }
            }

            mailSender.send(message);
            log.info("模板邮件已从 {} 发送至 {}", email, request.getTo());

            sendLog.setSendStatus(MailSendStatus.SUCCESS.getCode());
            sendLog.setSentAt(new Date());
            mailSendLogService.updateById(sendLog);

            return Result.success(sendLog, "发送成功");
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());
            sendLog.setSendStatus(MailSendStatus.FAILED.getCode());
            sendLog.setErrorMessage(e.getMessage());
            mailSendLogService.updateById(sendLog);
            return Result.fail("发送失败：" + e.getMessage());
        }
    }
}
