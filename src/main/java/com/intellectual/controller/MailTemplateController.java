package com.intellectual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellectual.annotation.RequirePermission;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.MailTemplate;
import com.intellectual.service.MailTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件模板表 前端控制器
 *
 * @author 陈创
 * @since 2026-07-23 19:09
 */
@RestController
@RequestMapping("/api/mail-template")
public class MailTemplateController {

    @Autowired
    private MailTemplateService mailTemplateService;

    /** 分页列表 */
    @RequirePermission("system:mailTemplate:list")
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String templateCode,
                       @RequestParam(required = false) String templateName,
                       @RequestParam(required = false) Integer enabled) {
        LambdaQueryWrapper<MailTemplate> wrapper = new LambdaQueryWrapper<MailTemplate>()
                .like(templateCode != null, MailTemplate::getTemplateCode, templateCode)
                .like(templateName != null, MailTemplate::getTemplateName, templateName)
                .eq(enabled != null, MailTemplate::getEnabled, enabled)
                .orderByDesc(MailTemplate::getCreateTime);
        List<MailTemplate> all = mailTemplateService.list(wrapper);
        int total = all.size();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<MailTemplate> page = from < total ? all.subList(from, to) : List.of();
        Map<String, Object> result = new HashMap<>();
        result.put("records", page);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    /** 全部列表（不分页） */
    @GetMapping("/all")
    public Result all() {
        return Result.success(mailTemplateService.list(
                new LambdaQueryWrapper<MailTemplate>().orderByDesc(MailTemplate::getCreateTime)));
    }

    /** 详情 */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        MailTemplate template = mailTemplateService.getById(id);
        if (template == null) {
            return Result.fail("模板不存在");
        }
        return Result.success(template);
    }

    /** 新增 */
    @RequirePermission("system:mailTemplate:add")
    @PostMapping
    public Result add(@RequestBody MailTemplate template) {
        mailTemplateService.save(template);
        return Result.success(template, "新增成功");
    }

    /** 修改 */
    @RequirePermission("system:mailTemplate:edit")
    @PutMapping
    public Result update(@RequestBody MailTemplate template) {
        if (template.getId() == null) {
            return Result.fail("ID不能为空");
        }
        mailTemplateService.updateById(template);
        return Result.success(template, "修改成功");
    }

    /** 删除 */
    @RequirePermission("system:mailTemplate:delete")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        mailTemplateService.removeById(id);
        return Result.successMsg("删除成功");
    }

    /** 批量删除 */
    @RequirePermission("system:mailTemplate:delete")
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        mailTemplateService.removeByIds(ids);
        return Result.successMsg("批量删除成功");
    }
}
