package com.intellectual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.MailSendAttachment;
import com.intellectual.model.entity.MailSendLog;
import com.intellectual.security.LoginUser;
import com.intellectual.service.MailSendAttachmentService;
import com.intellectual.service.MailSendLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件发送记录表 前端控制器
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
@RestController
@RequestMapping("/api/mail/log")
@RequiredArgsConstructor
public class MailSendLogController {

    private final MailSendLogService mailSendLogService;
    private final MailSendAttachmentService mailSendAttachmentService;

    /** 分页查询当前用户的发送记录 */
    @GetMapping("/page")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Page<MailSendLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MailSendLog> wrapper = new LambdaQueryWrapper<MailSendLog>()
                .eq(MailSendLog::getSenderUserId, loginUser.getUserId())
                .orderByDesc(MailSendLog::getCreateTime);
        mailSendLogService.page(page, wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        return Result.success(result);
    }

    /** 查询发送记录详情（含附件列表） */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        MailSendLog sendLog = mailSendLogService.getById(id);
        if (sendLog == null) {
            return Result.fail("记录不存在");
        }
        List<MailSendAttachment> attachments = mailSendAttachmentService.list(
                new LambdaQueryWrapper<MailSendAttachment>()
                        .eq(MailSendAttachment::getMailSendLogId, id));

        Map<String, Object> result = new HashMap<>();
        result.put("sendLog", sendLog);
        result.put("attachments", attachments);
        return Result.success(result);
    }
}