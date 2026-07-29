package com.intellectual.controller;

import com.intellectual.model.dto.MailRequest;
import com.intellectual.model.dto.Result;
import com.intellectual.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    @PostMapping("/sendMaill")
    public Result sendMail(@RequestParam(required = false) Long disclosureId,
                           @RequestParam String to,
                           @RequestParam String subject,
                           @RequestParam String content,
                           @RequestParam(required = false) String cc,
                           @RequestParam(required = false, defaultValue = "false") boolean isHtml,
                           @RequestParam(required = false) MultipartFile files,
                           @RequestParam(required = false) List<Long> disclosureAttachmentIds) {

        return mailService.sendMail(disclosureId, to, subject, content, cc, isHtml, files, disclosureAttachmentIds);
    }

    @PostMapping("sendMailWithTemplate")
    public Result sendMailWithTemplate(@RequestBody MailRequest request){
        return mailService.sendMailWithTemplate(request);
    }


}
