package com.intellectual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellectual.annotation.RequirePermission;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.PatentDisclosure;
import com.intellectual.service.PatentDisclosureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * T表（专利交底信息表）控制器
 */
@RestController
@RequestMapping("/api/ttable")
public class TtableController {

    @Autowired
    private PatentDisclosureService patentDisclosureService;

    /** 分页列表 */
    @RequirePermission("patent:disclosure:list")
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String disclosureName,
                       @RequestParam(required = false) String patentType,
                       @RequestParam(required = false) String patentStatus,
                       @RequestParam(required = false) String internalNo,
                       @RequestParam(required = false) String applicant) {
        LambdaQueryWrapper<PatentDisclosure> wrapper = new LambdaQueryWrapper<PatentDisclosure>()
                .like(disclosureName != null, PatentDisclosure::getDisclosureName, disclosureName)
                .eq(patentType != null, PatentDisclosure::getPatentType, patentType)
                .eq(patentStatus != null, PatentDisclosure::getPatentStatus, patentStatus)
                .eq(internalNo != null, PatentDisclosure::getInternalNo, internalNo)
                .like(applicant != null, PatentDisclosure::getApplicant, applicant)
                .orderByDesc(PatentDisclosure::getCreateTime);
        List<PatentDisclosure> all = patentDisclosureService.list(wrapper);
        int total = all.size();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<PatentDisclosure> page = from < total ? all.subList(from, to) : List.of();
        Map<String, Object> result = new HashMap<>();
        result.put("records", page);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    /** 详情 */
    @RequirePermission("patent:disclosure:query")
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        PatentDisclosure disclosure = patentDisclosureService.getById(id);
        if (disclosure == null) {
            return Result.fail("交底记录不存在");
        }
        return Result.success(disclosure);
    }

    /** 新增 */
    @RequirePermission("patent:disclosure:add")
    @PostMapping
    public Result add(@RequestBody PatentDisclosure disclosure) {
        patentDisclosureService.save(disclosure);
        return Result.success(disclosure, "新增成功");
    }

    /** 修改 */
    @RequirePermission("patent:disclosure:edit")
    @PutMapping
    public Result update(@RequestBody PatentDisclosure disclosure) {
        if (disclosure.getId() == null) {
            return Result.fail("ID不能为空");
        }
        patentDisclosureService.updateById(disclosure);
        return Result.success(disclosure, "修改成功");
    }

    /** 删除 */
    @RequirePermission("patent:disclosure:delete")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        patentDisclosureService.removeById(id);
        return Result.successMsg("删除成功");
    }

    /** 批量删除 */
    @RequirePermission("patent:disclosure:delete")
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        patentDisclosureService.removeByIds(ids);
        return Result.successMsg("批量删除成功");
    }

    /** 全部列表（不分页） */
    @RequirePermission("patent:disclosure:list")
    @GetMapping("/all")
    public Result all() {
        return Result.success(patentDisclosureService.list(
                new LambdaQueryWrapper<PatentDisclosure>().orderByDesc(PatentDisclosure::getCreateTime)));
    }
}
