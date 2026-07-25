package com.intellectual.service;

import com.intellectual.model.dto.MailRequest;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.Mail;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户邮箱表 服务类接口
 *
 * @author 陈创
 * @since 2026-07-23 19:09
 */
public interface MailService extends IService<Mail> {
    Result sendMail(String to, String subject, String content, String cc, boolean isHtml, MultipartFile files);

    Result sendMailWithTemplate(MailRequest request);
}
