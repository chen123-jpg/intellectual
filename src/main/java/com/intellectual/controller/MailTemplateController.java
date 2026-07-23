package com.intellectual.controller;

import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.MailTemplate;
import com.intellectual.service.MailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 邮件模板表 前端控制器
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
@RestController
@RequestMapping("/api/mail/template")
@RequiredArgsConstructor
public class MailTemplateController {

    private final MailTemplateService mailTemplateService;

    /** 查询所有启用的模板 */
    @GetMapping("/list")
    public Result<List<MailTemplate>> list() {
        return Result.success(mailTemplateService.listEnabled());
    }

    /** 按编码查询模板详情 */
    @GetMapping("/{templateCode}")
    public Result<MailTemplate> getByCode(@PathVariable String templateCode) {
        MailTemplate template = mailTemplateService.getByCode(templateCode);
        if (template == null) {
            return Result.fail("模板不存在");
        }
        return Result.success(template);
    }
}