package com.intellectual.controller;

import com.intellectual.model.dto.MailRequest;
import com.intellectual.model.dto.Result;
import com.intellectual.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    public Result sendMailWithTemplate(@RequestBody MailRequest request){
        return mailService.sendMailWithTemplate(request);
    }


}
