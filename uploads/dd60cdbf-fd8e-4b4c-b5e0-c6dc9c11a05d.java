package com.chen.intellectualproperty.service.impl;

import com.chen.intellectualproperty.model.entity.User;
import com.chen.intellectualproperty.model.enums.MailServerConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
public class MailService {

    public void sendMail(User sender, String to, String subject, String text,
                         List<MultipartFile> attachments) throws MessagingException, IOException {
        sendMailInternal(sender, to, null, subject, text, attachments, null);
    }

    /**
     * 使用磁盘附件发送（交底流程）。
     * @return 发件人邮箱
     */
    public String sendMailWithFiles(User sender, String to, String cc, String subject, String text,
                                    List<Path> filePaths, List<String> fileNames)
            throws MessagingException, IOException {
        return sendMailInternal(sender, to, cc, subject, text, null, pairFiles(filePaths, fileNames));
    }

    private List<NamedPath> pairFiles(List<Path> filePaths, List<String> fileNames) {
        if (filePaths == null || filePaths.isEmpty()) {
            return List.of();
        }
        java.util.ArrayList<NamedPath> list = new java.util.ArrayList<>();
        for (int i = 0; i < filePaths.size(); i++) {
            String name = (fileNames != null && i < fileNames.size() && fileNames.get(i) != null)
                    ? fileNames.get(i)
                    : filePaths.get(i).getFileName().toString();
            list.add(new NamedPath(filePaths.get(i), name));
        }
        return list;
    }

    private String sendMailInternal(User sender, String to, String cc, String subject, String text,
                                    List<MultipartFile> multipartFiles,
                                    List<NamedPath> pathFiles) throws MessagingException, IOException {
        String email = sender.getEmail();
        String authCode = sender.getAuthCode();
        String smtpHost = sender.getSmtpHost();
        Integer smtpPort = sender.getSmtpPort();

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
        if (mailSender.getPort() == 587) {
            props.put("mail.smtp.starttls.enable", "true");
        } else if (mailSender.getPort() == 465) {
            props.put("mail.smtp.ssl.enable", "true");
        }
        mailSender.setJavaMailProperties(props);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(email);
        helper.setTo(splitEmails(to));
        if (cc != null && !cc.isBlank()) {
            helper.setCc(splitEmails(cc));
        }
        helper.setSubject(subject);
        helper.setText(text, false);

        if (multipartFiles != null) {
            for (MultipartFile file : multipartFiles) {
                if (file == null || file.isEmpty()) continue;
                helper.addAttachment(file.getOriginalFilename(),
                        new ByteArrayResource(file.getBytes()), file.getContentType());
            }
        }
        if (pathFiles != null) {
            for (NamedPath np : pathFiles) {
                if (np.path() == null || !np.path().toFile().exists()) continue;
                helper.addAttachment(np.name(), new FileSystemResource(np.path().toFile()));
            }
        }

        mailSender.send(message);
        log.info("邮件已从 {} 发送至 {}", email, to);
        return email;
    }

    private String[] splitEmails(String emails) {
        return java.util.Arrays.stream(emails.split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    private record NamedPath(Path path, String name) {}
}
