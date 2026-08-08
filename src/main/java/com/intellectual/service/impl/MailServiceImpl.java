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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.io.FileSystemResource;

@Slf4j
@Service
public class MailServiceImpl extends ServiceImpl<MailMapper, Mail> implements MailService {

    private record BusinessMailContext(String type, String ref, String action) {}
    private final ThreadLocal<BusinessMailContext> businessMailContext = new ThreadLocal<>();

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

    public Result sendMail(String referenceId, String to, String subject, String content, String cc,
                           boolean isHtml, MultipartFile file, List<Long> disclosureAttachmentIds) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        String email = loginUser.getEmail();
        String authCode = loginUser.getAuthCode();
        String smtpHost = loginUser.getSmtpHost();
        Integer smtpPort = loginUser.getSmtpPort();

        if (authCode == null || smtpPort == null || smtpHost == null) {
            return Result.fail("未填写授权码，请在个人中心填写后重试");
        }

        // 1. 保存发送记录 (PENDING)
        MailSendLog sendLog = new MailSendLog();
        sendLog.setReferenceId(referenceId);
        sendLog.setFromEmail(email);
        sendLog.setToEmails(to);
        sendLog.setCcEmails(cc != null ? cc : "");
        sendLog.setSubject(subject);
        sendLog.setContent(content);
        sendLog.setSendStatus(MailSendStatus.PENDING.getCode());
        sendLog.setSenderUserId(loginUser.getUserId());
        sendLog.setSenderName(loginUser.getLoginName());
        BusinessMailContext businessContext = businessMailContext.get();
        if (businessContext != null) {
            sendLog.setBusinessType(businessContext.type());
            sendLog.setBusinessRef(businessContext.ref());
            sendLog.setBusinessAction(businessContext.action());
        }
        sendLog.setCreateTime(new Date());
        mailSendLogService.save(sendLog);

        // 2. 先上传文件到磁盘（邮件附件需要物理文件）
        String uploadedFileUrl = null;
        String originalFileName = null;
        Path diskPath = null;
        if (file != null && !file.isEmpty()) {
            try {
                Result uploadResult = uploadFileServiceImpl.upload(file);
                if (uploadResult.getCode() == 200) {
                    uploadedFileUrl = (String) uploadResult.getData();
                    String encodedName = UriComponentsBuilder.fromUriString(uploadedFileUrl)
                            .build().getQueryParams().getFirst("name");
                    originalFileName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8);
                    String pathPart = uploadedFileUrl.contains("?")
                            ? uploadedFileUrl.substring(0, uploadedFileUrl.indexOf("?"))
                            : uploadedFileUrl;
                    String fileId = pathPart.substring(pathPart.lastIndexOf("/") + 1);
                    diskPath = uploadPath.resolve(fileId).normalize();
                }
            } catch (Exception e) {
                log.error("附件上传失败", e);
            }
        }

        // 3. 发送邮件
        try {
            JavaMailSenderImpl mailSender = createMailSender(smtpHost, smtpPort, email, authCode);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(email);
            helper.setTo(to.split("[,;]"));
            if (cc != null && !cc.isBlank()) {
                helper.setCc(cc.split("[,;]"));
            }
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            if (file != null && !file.isEmpty()) {
                helper.addAttachment(file.getOriginalFilename(), file);
            }

            mailSender.send(message);
            log.info("邮件已从 {} 发送至 {}", email, to);

            // 4. 发送成功后才保存附件记录
            if (file != null && !file.isEmpty()) {
                String recordFileName = originalFileName != null ? originalFileName : file.getOriginalFilename();
                String recordFileUrl = uploadedFileUrl;
                String recordFilePath = diskPath != null ? diskPath.toString() : null;
                long recordFileSize;
                if (diskPath != null && Files.exists(diskPath)) {
                    recordFileSize = Files.size(diskPath);
                } else {
                    recordFileSize = file.getSize();
                }
                MailSendAttachment attachment = new MailSendAttachment();
                attachment.setMailSendLogId(sendLog.getId());
                attachment.setFileName(recordFileName);
                attachment.setFilePath(recordFilePath);
                attachment.setFileUrl(recordFileUrl);
                attachment.setFileSize(recordFileSize);
                attachment.setCreateTime(new Date());
                if (disclosureAttachmentIds != null && !disclosureAttachmentIds.isEmpty()) {
                    attachment.setDisclosureAttachmentId(disclosureAttachmentIds.get(0));
                }
                mailSendAttachmentService.save(attachment);
            }

            sendLog.setSendStatus(MailSendStatus.SUCCESS.getCode());
            sendLog.setSentAt(new Date());
            mailSendLogService.updateById(sendLog);

            return Result.success(sendLog, "发送成功");
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());

            // 发送失败，删除已上传的磁盘文件
            deleteUploadedFile(diskPath);

            sendLog.setSendStatus(MailSendStatus.FAILED.getCode());
            sendLog.setErrorMessage(e.getMessage());
            mailSendLogService.updateById(sendLog);
            return Result.fail(buildSendErrorMsg(e));
        }
    }

    @Override
    public Result sendBusinessMail(String referenceId, String to, String subject, String content,
                                   String businessType, String businessRef, String businessAction) {
        businessMailContext.set(new BusinessMailContext(businessType, businessRef, businessAction));
        try {
            return sendMail(referenceId, to, subject, content, null, true, null, null);
        } finally {
            businessMailContext.remove();
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

        // 1. 渲染主题和正文
        String subject;
        String content;
        if (request.getTemplateCode() != null && !request.getTemplateCode().isBlank()) {
            MailTemplate template = mailTemplateService.getOne(
                    new LambdaQueryWrapper<MailTemplate>()
                            .eq(MailTemplate::getTemplateCode, request.getTemplateCode())
                            .eq(MailTemplate::getEnabled, 1)
            );
            if (template == null) {
                return Result.fail("模板不存在或未启用");
            }
            Context context = new Context();
            if (request.getTemplateData() != null) {
                context.setVariables(request.getTemplateData());
            }
            subject = request.getSubject() != null && !request.getSubject().isBlank()
                    ? request.getSubject()
                    : templateEngine.process(template.getSubject(), context);
            content = request.getText() != null
                    ? request.getText()
                    : templateEngine.process(template.getContent(), context);
        } else {
            subject = request.getSubject();
            content = request.getText();
            if (subject == null || subject.isBlank()) {
                return Result.fail("主题不能为空");
            }
            if (content == null || content.isBlank()) {
                return Result.fail("正文不能为空");
            }
        }

        log.info("sendMailWithTemplate 请求参数: referenceId={}, to={}, templateCode={}, attachmentUrls={}, disclosureAttachmentIds={}",
                resolveReferenceId(request), request.getTo(), request.getTemplateCode(), request.getAttachmentUrls(), request.getDisclosureAttachmentIds());

        // 3. 保存发送记录 (PENDING)
        MailSendLog sendLog = new MailSendLog();
        sendLog.setReferenceId(resolveReferenceId(request));
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

        // 4. 发送邮件
        try {
            JavaMailSenderImpl mailSender = createMailSender(smtpHost, smtpPort, email, authCode);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(email);
            helper.setTo(request.getTo().split("[,;]"));
            if (request.getCc() != null && !request.getCc().isBlank()) {
                helper.setCc(request.getCc().split("[,;]"));
            }
            if (request.getBcc() != null && !request.getBcc().isBlank()) {
                helper.setBcc(request.getBcc().split("[,;]"));
            }
            helper.setSubject(subject);

            // 先处理附件/图片：图片 URL 替换为 <img> 标签后再设置正文
            if (request.getAttachmentUrls() != null) {
                for (String fileUrl : request.getAttachmentUrls()) {
                    try {
                        Path diskPath = resolvePath(fileUrl);
                        if (!Files.exists(diskPath)) continue;
                        String encodedName = UriComponentsBuilder.fromUriString(fileUrl)
                                .build().getQueryParams().getFirst("name");
                        String attachName = encodedName != null
                                ? URLDecoder.decode(encodedName, StandardCharsets.UTF_8)
                                : diskPath.getFileName().toString();
                        if (isImageFile(attachName)) {
                            String cid = "img-" + System.currentTimeMillis() + "-" + attachName.replaceAll("[^a-zA-Z0-9.]", "_");
                            helper.addInline(cid, new FileSystemResource(diskPath), inferMimeType(attachName));
                            String srcAttr = "src=\"" + fileUrl + "\"";
                            String imgTag = "<img src=\"cid:" + cid + "\" style=\"max-width:100%\" />";
                            if (content.contains(srcAttr)) {
                                // URL 已在 <img src="..."> 中，只替换 src 值
                                content = content.replace(srcAttr, "src=\"cid:" + cid + "\"");
                            } else if (content.contains(fileUrl)) {
                                // URL 作为纯文本出现在正文中，替换为 <img> 标签
                                content = content.replace(fileUrl, imgTag);
                            } else {
                                // 正文未引用该图片 URL：尊重编辑后的预览内容，不再追加
                                log.info("图片 URL 未在正文中出现，跳过内联: {}", fileUrl);
                            }
                            log.info("内联图片: {} -> cid:{}", fileUrl, cid);
                        } else {
                            helper.addAttachment(attachName, new FileSystemResource(diskPath));
                        }
                    } catch (Exception e) {
                        log.error("添加附件/图片到邮件失败: {}", fileUrl, e);
                    }
                }
            }
            helper.setText(content, true);

            mailSender.send(message);
            log.info("模板邮件已从 {} 发送至 {}", email, request.getTo());

            // 5. 发送成功后才保存附件记录
            if (request.getAttachmentUrls() != null && !request.getAttachmentUrls().isEmpty()) {
                List<Long> attachmentIdList = request.getDisclosureAttachmentIds();
                for (int i = 0; i < request.getAttachmentUrls().size(); i++) {
                    try {
                        Long disclosureAttachmentId = (attachmentIdList != null && i < attachmentIdList.size())
                                ? attachmentIdList.get(i) : null;
                        saveAttachmentRecord(sendLog.getId(), request.getAttachmentUrls().get(i), disclosureAttachmentId);
                    } catch (Exception e) {
                        log.error("附件记录保存失败: {}", request.getAttachmentUrls().get(i), e);
                    }
                }
            }

            sendLog.setSendStatus(MailSendStatus.SUCCESS.getCode());
            sendLog.setSentAt(new Date());
            mailSendLogService.updateById(sendLog);

            return Result.success(sendLog, "发送成功");
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());

            // 发送失败，删除预上传的磁盘文件
            if (request.getAttachmentUrls() != null) {
                for (String fileUrl : request.getAttachmentUrls()) {
                    deleteUploadedFile(resolvePath(fileUrl));
                }
            }

            sendLog.setSendStatus(MailSendStatus.FAILED.getCode());
            sendLog.setErrorMessage(e.getMessage());
            mailSendLogService.updateById(sendLog);
            return Result.fail(buildSendErrorMsg(e));
        }
    }

    @Override
    public Result renderPreview(MailRequest request) {
        if (request.getTemplateCode() == null || request.getTemplateCode().isBlank()) {
            return Result.fail("请选择模板");
        }
        MailTemplate template = mailTemplateService.getOne(
                new LambdaQueryWrapper<MailTemplate>()
                        .eq(MailTemplate::getTemplateCode, request.getTemplateCode())
                        .eq(MailTemplate::getEnabled, 1)
        );
        if (template == null) {
            return Result.fail("模板不存在或未启用");
        }
        Context context = new Context();
        if (request.getTemplateData() != null) {
            context.setVariables(request.getTemplateData());
        }
        String subject = templateEngine.process(template.getSubject(), context);
        String content = templateEngine.process(template.getContent(), context);
        Map<String, Object> result = new HashMap<>();
        result.put("subject", subject);
        result.put("content", content);
        return Result.success(result);
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

    private String resolveReferenceId(MailRequest request) {
        if (request.getReferenceId() != null && !request.getReferenceId().isBlank()) {
            return request.getReferenceId();
        }
        if (request.getInternalNo() != null && !request.getInternalNo().isBlank()) {
            return request.getInternalNo();
        }
        if (request.getDisclosureId() != null) {
            return String.valueOf(request.getDisclosureId());
        }
        return null;
    }

    private Path resolvePath(String fileUrl) {
        String pathPart = fileUrl.contains("?")
                ? fileUrl.substring(0, fileUrl.indexOf("?"))
                : fileUrl;
        String fileId = pathPart.substring(pathPart.lastIndexOf("/") + 1);
        return uploadPath.resolve(fileId).normalize();
    }

    private void saveAttachmentRecord(Long sendLogId, String fileUrl, Long disclosureAttachmentId) {
        String encodedName = UriComponentsBuilder.fromUriString(fileUrl)
                .build().getQueryParams().getFirst("name");
        String originalName = encodedName != null
                ? URLDecoder.decode(encodedName, StandardCharsets.UTF_8)
                : fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

        Path diskPath = resolvePath(fileUrl);
        long fileSize = 0;
        try {
            fileSize = Files.exists(diskPath) ? Files.size(diskPath) : 0;
        } catch (IOException e) {
            log.warn(e.getMessage());
        }

        MailSendAttachment attachment = new MailSendAttachment();
        attachment.setMailSendLogId(sendLogId);
        attachment.setDisclosureAttachmentId(disclosureAttachmentId);
        attachment.setFileName(originalName);
        attachment.setFilePath(diskPath.toString());
        attachment.setFileUrl(fileUrl);
        attachment.setFileSize(fileSize);
        attachment.setCreateTime(new Date());
        mailSendAttachmentService.save(attachment);
    }

    private void deleteUploadedFile(Path diskPath) {
        if (diskPath != null) {
            try {
                Files.deleteIfExists(diskPath);
                log.info("已删除上传文件: {}", diskPath);
            } catch (Exception e) {
                log.warn("删除上传文件失败: {}", diskPath, e);
            }
        }
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

    private String buildSendErrorMsg(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            return "发送失败，请稍后重试";
        }
        String lower = msg.toLowerCase();
        if (lower.contains("authentication") || lower.contains("535")
                || lower.contains("login fail") || lower.contains("authorization code")) {
            return "邮件认证失败：邮箱地址或授权码错误，请检查SMTP配置";
        }
        if (lower.contains("connect") || lower.contains("timeout") || lower.contains("unknown host")) {
            return "邮件服务器连接失败：请检查SMTP服务器地址和端口";
        }
        if (lower.contains("550") || lower.contains("non-existent account")
                || lower.contains("recipient") && lower.contains("check")) {
            return "发送失败：收件人邮箱地址不存在，请检查收件人地址";
        }
        return "发送失败：" + msg;
    }
}
