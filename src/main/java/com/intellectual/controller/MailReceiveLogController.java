package com.intellectual.controller;

import com.intellectual.model.dto.Result;
import com.intellectual.service.MailReceiveLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/mail-receive-log")
public class MailReceiveLogController {

    @Autowired
    private MailReceiveLogService mailReceiveLogService;

    @GetMapping("/list")
    public Result getPage(@RequestParam Long userId,
                          @RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize) {
        return mailReceiveLogService.getPage(userId, pageNum, pageSize);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return mailReceiveLogService.getById(id);
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return mailReceiveLogService.deleteById(id);
    }

    @DeleteMapping("/batch")
    public Result batchDelete(@RequestBody List<Long> ids) {
        return mailReceiveLogService.batchDelete(ids);
    }
}
