package com.intellectual.controller;

import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.MailSendAttachment;
import com.intellectual.model.entity.MailSendLog;
import com.intellectual.model.entity.MailTemplate;
import com.intellectual.model.enums.MailSendStatus;
import com.intellectual.security.LoginUser;
import com.intellectual.security.UserDetailsServiceImpl;
import com.intellectual.service.MailSendAttachmentService;
import com.intellectual.service.MailSendLogService;
import com.intellectual.service.MailService;
import com.intellectual.service.MailTemplateService;
import com.intellectual.service.impl.UploadFileServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Wrapper;

@Slf4j
@RestController
@RequestMapping("api/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/sendMaill")
    public Result sendMail(@RequestParam String to,
                           @RequestParam String subject,
                           @RequestParam String content,
                           @RequestParam (required = false) String cc,
                           @RequestParam (required = false , defaultValue = "false") boolean isHtml,
                           @RequestParam (required = false)MultipartFile files){

        return mailService.sendMail(to,subject,content,cc,isHtml,files);
    }

    @PostMapping("sendMailWithTemplate")
    public Result sendMailWithTemplate(){
        return mailService.sendMailWithTemplate();
    }


}
