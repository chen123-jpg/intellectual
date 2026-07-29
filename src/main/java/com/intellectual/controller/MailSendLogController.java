package com.intellectual.controller;

import com.intellectual.model.dto.Result;
import com.intellectual.service.MailSendLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/mail-send-log")
public class MailSendLogController {

    @Autowired
    private MailSendLogService mailSendLogService;

    @GetMapping
    public Result getLogByDisclosureId(Long disclosureId){
        return mailSendLogService.getlogById(disclosureId);
    }
}
