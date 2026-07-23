// src/main/java/com/intellectual/controller/MailController.java
package com.intellectual.controller;

import com.intellectual.model.dto.MailRequest;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.MailSendLog;
import com.intellectual.security.LoginUser;
import com.intellectual.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    /** 发送邮件（支持附件），自动记录发送日志和附件记录 */
    @PostMapping("/send")
    public Result<MailSendLog> sendMail(@ModelAttribute MailRequest request) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        log.info("用户 {} 发起邮件发送，目标: {}", loginUser.getLoginName(), request.getTo());

        try {
            MailSendLog sendLog = mailService.sendMail(loginUser, request.getTo(),
                    request.getCc(), request.getSubject(), request.getText(), request.getAttachments());
            return Result.success(sendLog, "发送成功");
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            return Result.fail("发送失败：" + e.getMessage());
        }
    }
}