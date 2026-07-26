package com.intellectual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellectual.annotation.RequirePermission;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.ApplicationPackage;
import com.intellectual.service.ApplicationPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 申请包控制器（XML包与五书WORD分条目）
 */
@RestController
@RequestMapping("/api/application-package")
public class ApplicationPackageController {

    @Autowired
    private ApplicationPackageService applicationPackageService;

    // ═══════════════════════════════════════════════
    // 基本 CRUD
    // ═══════════════════════════════════════════════

    /** 分页列表 */
    @RequirePermission("patent:applicationPackage:list")
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Long disclosureId,
                       @RequestParam(required = false) String packageType,
                       @RequestParam(required = false) String confirmStatus,
                       @RequestParam(required = false) String internalNo) {
        LambdaQueryWrapper<ApplicationPackage> wrapper = new LambdaQueryWrapper<ApplicationPackage>()
                .eq(disclosureId != null, ApplicationPackage::getDisclosureId, disclosureId)
                .eq(packageType != null, ApplicationPackage::getPackageType, packageType)
                .eq(confirmStatus != null, ApplicationPackage::getConfirmStatus, confirmStatus)
                .eq(internalNo != null, ApplicationPackage::getInternalNo, internalNo)
                .orderByDesc(ApplicationPackage::getCreateTime);
        return pageResult(applicationPackageService.list(wrapper), pageNum, pageSize);
    }

    /** 全部列表（不分页） */
    @RequirePermission("patent:applicationPackage:list")
    @GetMapping("/all")
    public Result all() {
        return Result.success(applicationPackageService.list(
                new LambdaQueryWrapper<ApplicationPackage>().orderByDesc(ApplicationPackage::getCreateTime)));
    }

    /** 详情 */
    @RequirePermission("patent:applicationPackage:query")
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        ApplicationPackage entity = applicationPackageService.getById(id);
        return entity != null ? Result.success(entity) : Result.fail("申请包不存在");
    }

    /** 新增 */
    @RequirePermission("patent:applicationPackage:add")
    @PostMapping
    public Result add(@RequestBody ApplicationPackage entity) {
        if (entity.getUploadTime() == null) {
            entity.setUploadTime(new Date());
        }
        if (entity.getVersionNo() == null) {
            entity.setVersionNo(1);
        }
        if (entity.getIsCurrent() == null) {
            entity.setIsCurrent(1);
        }
        if (entity.getConfirmStatus() == null) {
            entity.setConfirmStatus("UNCONFIRMED");
        }
        applicationPackageService.save(entity);
        return Result.success(entity, "新增成功");
    }

    /** 修改 */
    @RequirePermission("patent:applicationPackage:edit")
    @PutMapping
    public Result update(@RequestBody ApplicationPackage entity) {
        if (entity.getId() == null) {
            return Result.fail("ID不能为空");
        }
        applicationPackageService.updateById(entity);
        return Result.success(entity, "修改成功");
    }

    /** 删除 */
    @RequirePermission("patent:applicationPackage:delete")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        applicationPackageService.removeById(id);
        return Result.successMsg("删除成功");
    }

    /** 批量删除 */
    @RequirePermission("patent:applicationPackage:delete")
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        applicationPackageService.removeByIds(ids);
        return Result.successMsg("批量删除成功");
    }

    // ═══════════════════════════════════════════════
    // 业务方法
    // ═══════════════════════════════════════════════

    /** 按交底ID查询申请包 */
    @RequirePermission("patent:applicationPackage:list")
    @GetMapping("/by-disclosure/{disclosureId}")
    public Result listByDisclosure(@PathVariable Long disclosureId) {
        List<ApplicationPackage> list = applicationPackageService.list(
                new LambdaQueryWrapper<ApplicationPackage>()
                        .eq(ApplicationPackage::getDisclosureId, disclosureId)
                        .orderByDesc(ApplicationPackage::getCreateTime));
        return Result.success(list);
    }

    /** 确认申请包（流程确认） */
    @RequirePermission("patent:applicationPackage:edit")
    @PutMapping("/{id}/confirm")
    public Result confirm(@PathVariable Long id,
                          @RequestParam Long confirmUserId,
                          @RequestParam String confirmUserName) {
        ApplicationPackage entity = applicationPackageService.getById(id);
        if (entity == null) {
            return Result.fail("申请包不存在");
        }
        entity.setConfirmStatus("CONFIRMED");
        entity.setConfirmUserId(confirmUserId);
        entity.setConfirmUserName(confirmUserName);
        entity.setConfirmTime(new Date());
        applicationPackageService.updateById(entity);
        return Result.success(entity, "确认成功");
    }

    // ═══════════════════════════════════════════════
    // 分页工具方法
    // ═══════════════════════════════════════════════

    private <T> Result pageResult(List<T> all, int pageNum, int pageSize) {
        int total = all.size();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<T> page = from < total ? all.subList(from, to) : List.of();
        Map<String, Object> result = new HashMap<>();
        result.put("records", page);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }
}
