package com.intellectual.controller;

import com.intellectual.model.dto.Result;
import com.intellectual.service.MailSendLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/mail-send-log")
public class MailSendLogController {

    @Autowired
    private MailSendLogService mailSendLogService;

    @GetMapping
    public Result getLogByReferenceId(@RequestParam(required = false) String referenceId) {
        return mailSendLogService.getLogByReferenceId(referenceId);
    }

    @GetMapping("/list")
    public Result getPage(@RequestParam Long userId,
                          @RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize) {
        return mailSendLogService.getPage(userId, pageNum, pageSize);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return mailSendLogService.getById(id);
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return mailSendLogService.deleteById(id);
    }

    @DeleteMapping("/batch")
    public Result batchDelete(@RequestBody List<Long> ids) {
        return mailSendLogService.batchDelete(ids);
    }

    @PostMapping("/resend/{id}")
    public Result resend(@PathVariable Long id) {
        return mailSendLogService.resend(id);
    }
}
