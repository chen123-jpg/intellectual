package com.intellectual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellectual.annotation.RequirePermission;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.PatentIntermediateChange;
import com.intellectual.model.entity.PatentNewApplication;
import com.intellectual.model.entity.PatentPct;
import com.intellectual.model.entity.PatentReexamination;
import com.intellectual.model.entity.PatentSupplementary;
import com.intellectual.service.PatentIntermediateChangeService;
import com.intellectual.service.PatentNewApplicationService;
import com.intellectual.service.PatentPctService;
import com.intellectual.service.PatentReexaminationService;
import com.intellectual.service.PatentSupplementaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * P表控制器 —— 新申请、补漏、PCT、中间著变、复审无效
 */
@RestController
@RequestMapping("/api/ptable")
public class PtableController {

    @Autowired
    private PatentNewApplicationService newApplicationService;

    @Autowired
    private PatentSupplementaryService supplementaryService;

    @Autowired
    private PatentPctService pctService;

    @Autowired
    private PatentIntermediateChangeService intermediateChangeService;

    @Autowired
    private PatentReexaminationService reexaminationService;

    // ═══════════════════════════════════════════════
    // 新申请 (patent_new_application)
    // ═══════════════════════════════════════════════

    @RequirePermission("patent:newApplication:list")
    @GetMapping("/new-application/list")
    public Result listNewApplication(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                     @RequestParam(required = false) String patentName,
                                     @RequestParam(required = false) String applicationNo,
                                     @RequestParam(required = false) String patentType,
                                     @RequestParam(required = false) String applicant,
                                     @RequestParam(required = false) String internalNo,
                                     @RequestParam(required = false) String inventor,
                                     @RequestParam(required = false) String sponsor,
                                     @RequestParam(required = false) String agent,
                                     @RequestParam(required = false) String notification,
                                     @RequestParam(required = false) String preExamMark,
                                     @RequestParam(required = false) String paymentDate,
                                     @RequestParam(required = false) String dasCode,
                                     @RequestParam(required = false) String applicationDateStart,
                                     @RequestParam(required = false) String applicationDateEnd,
                                     @RequestParam(required = false) String issueDateStart,
                                     @RequestParam(required = false) String issueDateEnd,
                                     @RequestParam(required = false) String paymentDeadlineStart,
                                     @RequestParam(required = false) String paymentDeadlineEnd,
                                     @RequestParam(required = false) String createTimeStart,
                                     @RequestParam(required = false) String createTimeEnd) {
        LambdaQueryWrapper<PatentNewApplication> wrapper = new LambdaQueryWrapper<PatentNewApplication>()
                .like(patentName != null && !patentName.isBlank(), PatentNewApplication::getPatentName, patentName)
                .like(applicationNo != null && !applicationNo.isBlank(), PatentNewApplication::getApplicationNo, applicationNo)
                .eq(patentType != null && !patentType.isBlank(), PatentNewApplication::getPatentType, patentType)
                .like(applicant != null && !applicant.isBlank(), PatentNewApplication::getApplicant, applicant)
                .like(internalNo != null && !internalNo.isBlank(), PatentNewApplication::getInternalNo, internalNo)
                .like(inventor != null && !inventor.isBlank(), PatentNewApplication::getInventor, inventor)
                .like(sponsor != null && !sponsor.isBlank(), PatentNewApplication::getSponsor, sponsor)
                .like(agent != null && !agent.isBlank(), PatentNewApplication::getAgent, agent)
                .like(notification != null && !notification.isBlank(), PatentNewApplication::getNotification, notification)
                .like(preExamMark != null && !preExamMark.isBlank(), PatentNewApplication::getPreExamMark, preExamMark)
                .like(paymentDate != null && !paymentDate.isBlank(), PatentNewApplication::getPaymentDate, paymentDate)
                .like(dasCode != null && !dasCode.isBlank(), PatentNewApplication::getDasCode, dasCode)
                .ge(applicationDateStart != null && !applicationDateStart.isBlank(), PatentNewApplication::getApplicationDate, applicationDateStart)
                .le(applicationDateEnd != null && !applicationDateEnd.isBlank(), PatentNewApplication::getApplicationDate, applicationDateEnd)
                .ge(issueDateStart != null && !issueDateStart.isBlank(), PatentNewApplication::getIssueDate, issueDateStart)
                .le(issueDateEnd != null && !issueDateEnd.isBlank(), PatentNewApplication::getIssueDate, issueDateEnd)
                .ge(paymentDeadlineStart != null && !paymentDeadlineStart.isBlank(), PatentNewApplication::getPaymentDeadline, paymentDeadlineStart)
                .le(paymentDeadlineEnd != null && !paymentDeadlineEnd.isBlank(), PatentNewApplication::getPaymentDeadline, paymentDeadlineEnd)
                .ge(createTimeStart != null && !createTimeStart.isBlank(), PatentNewApplication::getCreateTime, createTimeStart)
                .le(createTimeEnd != null && !createTimeEnd.isBlank(), PatentNewApplication::getCreateTime, createTimeEnd)
                .orderByDesc(PatentNewApplication::getCreateTime);
        return pageResult(newApplicationService.list(wrapper), pageNum, pageSize);
    }

    @RequirePermission("patent:newApplication:list")
    @GetMapping("/new-application/all")
    public Result allNewApplication() {
        return Result.success(newApplicationService.list(
                new LambdaQueryWrapper<PatentNewApplication>().orderByDesc(PatentNewApplication::getCreateTime)));
    }

    @RequirePermission("patent:newApplication:query")
    @GetMapping("/new-application/{id}")
    public Result getNewApplication(@PathVariable Long id) {
        PatentNewApplication entity = newApplicationService.getById(id);
        return entity != null ? Result.success(entity) : Result.fail("记录不存在");
    }

    @RequirePermission("patent:newApplication:add")
    @PostMapping("/new-application")
    public Result addNewApplication(@RequestBody PatentNewApplication entity) {
        newApplicationService.save(entity);
        return Result.success(entity, "新增成功");
    }

    @RequirePermission("patent:newApplication:edit")
    @PutMapping("/new-application")
    public Result updateNewApplication(@RequestBody PatentNewApplication entity) {
        if (entity.getId() == null) return Result.fail("ID不能为空");
        newApplicationService.updateById(entity);
        return Result.success(entity, "修改成功");
    }

    @RequirePermission("patent:newApplication:delete")
    @DeleteMapping("/new-application/{id}")
    public Result deleteNewApplication(@PathVariable Long id) {
        newApplicationService.removeById(id);
        return Result.successMsg("删除成功");
    }

    @RequirePermission("patent:newApplication:delete")
    @DeleteMapping("/new-application/batch")
    public Result deleteBatchNewApplication(@RequestBody List<Long> ids) {
        newApplicationService.removeByIds(ids);
        return Result.successMsg("批量删除成功");
    }

    // ═══════════════════════════════════════════════
    // 补漏 (patent_supplementary)
    // ═══════════════════════════════════════════════

    @RequirePermission("patent:supplementary:list")
    @GetMapping("/supplementary/list")
    public Result listSupplementary(@RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(required = false) String patentName,
                                    @RequestParam(required = false) String applicationNo,
                                    @RequestParam(required = false) String applicant) {
        LambdaQueryWrapper<PatentSupplementary> wrapper = new LambdaQueryWrapper<PatentSupplementary>()
                .like(patentName != null, PatentSupplementary::getPatentName, patentName)
                .eq(applicationNo != null, PatentSupplementary::getApplicationNo, applicationNo)
                .like(applicant != null, PatentSupplementary::getApplicant, applicant)
                .orderByDesc(PatentSupplementary::getCreateTime);
        return pageResult(supplementaryService.list(wrapper), pageNum, pageSize);
    }

    @RequirePermission("patent:supplementary:list")
    @GetMapping("/supplementary/all")
    public Result allSupplementary() {
        return Result.success(supplementaryService.list(
                new LambdaQueryWrapper<PatentSupplementary>().orderByDesc(PatentSupplementary::getCreateTime)));
    }

    @RequirePermission("patent:supplementary:query")
    @GetMapping("/supplementary/{id}")
    public Result getSupplementary(@PathVariable Long id) {
        PatentSupplementary entity = supplementaryService.getById(id);
        return entity != null ? Result.success(entity) : Result.fail("记录不存在");
    }

    @RequirePermission("patent:supplementary:add")
    @PostMapping("/supplementary")
    public Result addSupplementary(@RequestBody PatentSupplementary entity) {
        supplementaryService.save(entity);
        return Result.success(entity, "新增成功");
    }

    @RequirePermission("patent:supplementary:edit")
    @PutMapping("/supplementary")
    public Result updateSupplementary(@RequestBody PatentSupplementary entity) {
        if (entity.getId() == null) return Result.fail("ID不能为空");
        supplementaryService.updateById(entity);
        return Result.success(entity, "修改成功");
    }

    @RequirePermission("patent:supplementary:delete")
    @DeleteMapping("/supplementary/{id}")
    public Result deleteSupplementary(@PathVariable Long id) {
        supplementaryService.removeById(id);
        return Result.successMsg("删除成功");
    }

    @RequirePermission("patent:supplementary:delete")
    @DeleteMapping("/supplementary/batch")
    public Result deleteBatchSupplementary(@RequestBody List<Long> ids) {
        supplementaryService.removeByIds(ids);
        return Result.successMsg("批量删除成功");
    }

    // ═══════════════════════════════════════════════
    // PCT (patent_pct)
    // ═══════════════════════════════════════════════

    @RequirePermission("patent:pct:list")
    @GetMapping("/pct/list")
    public Result listPct(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          @RequestParam(required = false) String applicationName,
                          @RequestParam(required = false) String applicationNo,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) String applicant) {
        LambdaQueryWrapper<PatentPct> wrapper = new LambdaQueryWrapper<PatentPct>()
                .like(applicationName != null, PatentPct::getApplicationName, applicationName)
                .eq(applicationNo != null, PatentPct::getApplicationNo, applicationNo)
                .eq(status != null, PatentPct::getStatus, status)
                .like(applicant != null, PatentPct::getApplicant, applicant)
                .orderByDesc(PatentPct::getCreateTime);
        return pageResult(pctService.list(wrapper), pageNum, pageSize);
    }

    @RequirePermission("patent:pct:list")
    @GetMapping("/pct/all")
    public Result allPct() {
        return Result.success(pctService.list(
                new LambdaQueryWrapper<PatentPct>().orderByDesc(PatentPct::getCreateTime)));
    }

    @RequirePermission("patent:pct:query")
    @GetMapping("/pct/{id}")
    public Result getPct(@PathVariable Long id) {
        PatentPct entity = pctService.getById(id);
        return entity != null ? Result.success(entity) : Result.fail("记录不存在");
    }

    @RequirePermission("patent:pct:add")
    @PostMapping("/pct")
    public Result addPct(@RequestBody PatentPct entity) {
        pctService.save(entity);
        return Result.success(entity, "新增成功");
    }

    @RequirePermission("patent:pct:edit")
    @PutMapping("/pct")
    public Result updatePct(@RequestBody PatentPct entity) {
        if (entity.getId() == null) return Result.fail("ID不能为空");
        pctService.updateById(entity);
        return Result.success(entity, "修改成功");
    }

    @RequirePermission("patent:pct:delete")
    @DeleteMapping("/pct/{id}")
    public Result deletePct(@PathVariable Long id) {
        pctService.removeById(id);
        return Result.successMsg("删除成功");
    }

    @RequirePermission("patent:pct:delete")
    @DeleteMapping("/pct/batch")
    public Result deleteBatchPct(@RequestBody List<Long> ids) {
        pctService.removeByIds(ids);
        return Result.successMsg("批量删除成功");
    }

    // ═══════════════════════════════════════════════
    // 中间著变 (patent_intermediate_change)
    // ═══════════════════════════════════════════════

    @RequirePermission("patent:intermediateChange:list")
    @GetMapping("/intermediate-change/list")
    public Result listIntermediateChange(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) String patentName,
                                         @RequestParam(required = false) String applicationNo,
                                         @RequestParam(required = false) String businessType,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) String applicant) {
        LambdaQueryWrapper<PatentIntermediateChange> wrapper = new LambdaQueryWrapper<PatentIntermediateChange>()
                .like(patentName != null, PatentIntermediateChange::getPatentName, patentName)
                .eq(applicationNo != null, PatentIntermediateChange::getApplicationNo, applicationNo)
                .eq(businessType != null, PatentIntermediateChange::getBusinessType, businessType)
                .eq(status != null, PatentIntermediateChange::getStatus, status)
                .like(applicant != null, PatentIntermediateChange::getApplicant, applicant)
                .orderByDesc(PatentIntermediateChange::getCreateTime);
        return pageResult(intermediateChangeService.list(wrapper), pageNum, pageSize);
    }

    @RequirePermission("patent:intermediateChange:list")
    @GetMapping("/intermediate-change/all")
    public Result allIntermediateChange() {
        return Result.success(intermediateChangeService.list(
                new LambdaQueryWrapper<PatentIntermediateChange>().orderByDesc(PatentIntermediateChange::getCreateTime)));
    }

    @RequirePermission("patent:intermediateChange:query")
    @GetMapping("/intermediate-change/{id}")
    public Result getIntermediateChange(@PathVariable Long id) {
        PatentIntermediateChange entity = intermediateChangeService.getById(id);
        return entity != null ? Result.success(entity) : Result.fail("记录不存在");
    }

    @RequirePermission("patent:intermediateChange:add")
    @PostMapping("/intermediate-change")
    public Result addIntermediateChange(@RequestBody PatentIntermediateChange entity) {
        intermediateChangeService.save(entity);
        return Result.success(entity, "新增成功");
    }

    @RequirePermission("patent:intermediateChange:edit")
    @PutMapping("/intermediate-change")
    public Result updateIntermediateChange(@RequestBody PatentIntermediateChange entity) {
        if (entity.getId() == null) return Result.fail("ID不能为空");
        intermediateChangeService.updateById(entity);
        return Result.success(entity, "修改成功");
    }

    @RequirePermission("patent:intermediateChange:delete")
    @DeleteMapping("/intermediate-change/{id}")
    public Result deleteIntermediateChange(@PathVariable Long id) {
        intermediateChangeService.removeById(id);
        return Result.successMsg("删除成功");
    }

    @RequirePermission("patent:intermediateChange:delete")
    @DeleteMapping("/intermediate-change/batch")
    public Result deleteBatchIntermediateChange(@RequestBody List<Long> ids) {
        intermediateChangeService.removeByIds(ids);
        return Result.successMsg("批量删除成功");
    }

    // ═══════════════════════════════════════════════
    // 复审无效 (patent_reexamination)
    // ═══════════════════════════════════════════════

    @RequirePermission("patent:reexamination:list")
    @GetMapping("/reexamination/list")
    public Result listReexamination(@RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(required = false) String patentName,
                                    @RequestParam(required = false) String applicationNo,
                                    @RequestParam(required = false) String patentType,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) String status) {
        LambdaQueryWrapper<PatentReexamination> wrapper = new LambdaQueryWrapper<PatentReexamination>()
                .like(patentName != null, PatentReexamination::getPatentName, patentName)
                .eq(applicationNo != null, PatentReexamination::getApplicationNo, applicationNo)
                .eq(patentType != null, PatentReexamination::getPatentType, patentType)
                .eq(category != null, PatentReexamination::getCategory, category)
                .orderByDesc(PatentReexamination::getCreateTime);
        return pageResult(reexaminationService.list(wrapper), pageNum, pageSize);
    }

    @RequirePermission("patent:reexamination:list")
    @GetMapping("/reexamination/all")
    public Result allReexamination() {
        return Result.success(reexaminationService.list(
                new LambdaQueryWrapper<PatentReexamination>().orderByDesc(PatentReexamination::getCreateTime)));
    }

    @RequirePermission("patent:reexamination:query")
    @GetMapping("/reexamination/{id}")
    public Result getReexamination(@PathVariable Long id) {
        PatentReexamination entity = reexaminationService.getById(id);
        return entity != null ? Result.success(entity) : Result.fail("记录不存在");
    }

    @RequirePermission("patent:reexamination:add")
    @PostMapping("/reexamination")
    public Result addReexamination(@RequestBody PatentReexamination entity) {
        reexaminationService.save(entity);
        return Result.success(entity, "新增成功");
    }

    @RequirePermission("patent:reexamination:edit")
    @PutMapping("/reexamination")
    public Result updateReexamination(@RequestBody PatentReexamination entity) {
        if (entity.getId() == null) return Result.fail("ID不能为空");
        reexaminationService.updateById(entity);
        return Result.success(entity, "修改成功");
    }

    @RequirePermission("patent:reexamination:delete")
    @DeleteMapping("/reexamination/{id}")
    public Result deleteReexamination(@PathVariable Long id) {
        reexaminationService.removeById(id);
        return Result.successMsg("删除成功");
    }

    @RequirePermission("patent:reexamination:delete")
    @DeleteMapping("/reexamination/batch")
    public Result deleteBatchReexamination(@RequestBody List<Long> ids) {
        reexaminationService.removeByIds(ids);
        return Result.successMsg("批量删除成功");
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
