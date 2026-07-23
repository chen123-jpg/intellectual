// src/main/java/com/intellectual/service/MailService.java
package com.intellectual.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.intellectual.model.entity.Mail;
import com.intellectual.model.entity.MailSendLog;
import com.intellectual.security.LoginUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface MailService extends IService<Mail> {

    /**
     * 发送普通邮件（支持 MultipartFile 附件），返回发送记录
     */
    MailSendLog sendMail(LoginUser sender, String to, String cc, String subject, String text,
                         List<MultipartFile> attachments) throws Exception;

    /**
     * 发送带磁盘附件的邮件（用于交底流程），返回发送记录
     */
    MailSendLog sendMailWithFiles(LoginUser sender, String to, String cc, String subject, String text,
                                  List<Path> filePaths, List<String> fileNames) throws Exception;
}