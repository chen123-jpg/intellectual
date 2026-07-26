package com.intellectual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellectual.annotation.RequirePermission;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.*;
import com.intellectual.service.*;
import com.intellectual.service.impl.UploadFileServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * T表（专利交底信息表）控制器
 * <p>
 * 管理交底主数据、附件、申请包、状态日志、费用、开票等关联信息
 * </p>
 */
@RestController
@RequestMapping("/api/ttable")
public class TtableController {

    @Autowired
    private PatentDisclosureService patentDisclosureService;

    @Autowired
    private DisclosureAttachmentService disclosureAttachmentService;

    @Autowired
    private DisclosureStatusLogService disclosureStatusLogService;

    @Autowired
    private FeePaymentService feePaymentService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private UploadFileServiceImpl uploadFileService;

    @Autowired
    private ApplicationPackageService applicationPackageService;

    // ═══════════════════════════════════════════════
    // 交底主数据 CRUD
    // ═══════════════════════════════════════════════

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
        return pageResult(patentDisclosureService.list(wrapper), pageNum, pageSize);
    }

    /** 高级搜索（支持更多筛选条件） */
    @RequirePermission("patent:disclosure:list")
    @PostMapping("/search")
    public Result search(@RequestParam(defaultValue = "1") Integer pageNum,
                         @RequestParam(defaultValue = "10") Integer pageSize,
                         @RequestBody(required = false) PatentDisclosure query) {
        LambdaQueryWrapper<PatentDisclosure> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.like(query.getDisclosureName() != null, PatentDisclosure::getDisclosureName, query.getDisclosureName())
                    .eq(query.getPatentType() != null, PatentDisclosure::getPatentType, query.getPatentType())
                    .eq(query.getPatentStatus() != null, PatentDisclosure::getPatentStatus, query.getPatentStatus())
                    .eq(query.getInternalNo() != null, PatentDisclosure::getInternalNo, query.getInternalNo())
                    .eq(query.getTempNo() != null, PatentDisclosure::getTempNo, query.getTempNo())
                    .like(query.getApplicant() != null, PatentDisclosure::getApplicant, query.getApplicant())
                    .like(query.getInventor() != null, PatentDisclosure::getInventor, query.getInventor())
                    .like(query.getAgent() != null, PatentDisclosure::getAgent, query.getAgent())
                    .like(query.getSponsor() != null, PatentDisclosure::getSponsor, query.getSponsor())
                    .eq(query.getSponsorUserId() != null, PatentDisclosure::getSponsorUserId, query.getSponsorUserId())
                    .like(query.getContactPerson() != null, PatentDisclosure::getContactPerson, query.getContactPerson())
                    .eq(query.getSyncedToPatent() != null, PatentDisclosure::getSyncedToPatent, query.getSyncedToPatent());
        }
        wrapper.orderByDesc(PatentDisclosure::getCreateTime);
        return pageResult(patentDisclosureService.list(wrapper), pageNum, pageSize);
    }

    /** 全部列表（不分页） */
    @RequirePermission("patent:disclosure:list")
    @GetMapping("/all")
    public Result all() {
        return Result.success(patentDisclosureService.list(
                new LambdaQueryWrapper<PatentDisclosure>().orderByDesc(PatentDisclosure::getCreateTime)));
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

    /** 详情（含关联附件、状态日志、费用、开票、申请包） */
    @RequirePermission("patent:disclosure:query")
    @GetMapping("/{id}/detail")
    public Result detail(@PathVariable Long id) {
        PatentDisclosure disclosure = patentDisclosureService.getById(id);
        if (disclosure == null) {
            return Result.fail("交底记录不存在");
        }
        List<DisclosureAttachment> attachments = disclosureAttachmentService.list(
                new LambdaQueryWrapper<DisclosureAttachment>()
                        .eq(DisclosureAttachment::getDisclosureId, id)
                        .eq(DisclosureAttachment::getDeleted, 0)
                        .orderByAsc(DisclosureAttachment::getSortNo));
        List<DisclosureStatusLog> statusLogs = disclosureStatusLogService.list(
                new LambdaQueryWrapper<DisclosureStatusLog>()
                        .eq(DisclosureStatusLog::getDisclosureId, id)
                        .orderByDesc(DisclosureStatusLog::getCreateTime));
        List<FeePayment> fees = feePaymentService.list(
                new LambdaQueryWrapper<FeePayment>()
                        .eq(FeePayment::getDisclosureId, id)
                        .orderByDesc(FeePayment::getCreateTime));
        List<Invoice> invoices = invoiceService.list(
                new LambdaQueryWrapper<Invoice>()
                        .eq(Invoice::getDisclosureId, id)
                        .orderByDesc(Invoice::getCreateTime));
        List<ApplicationPackage> packages = applicationPackageService.list(
                new LambdaQueryWrapper<ApplicationPackage>()
                        .eq(ApplicationPackage::getDisclosureId, id)
                        .orderByDesc(ApplicationPackage::getCreateTime));
        Map<String, Object> result = new HashMap<>();
        result.put("disclosure", disclosure);
        result.put("attachments", attachments);
        result.put("statusLogs", statusLogs);
        result.put("fees", fees);
        result.put("invoices", invoices);
        result.put("packages", packages);
        return Result.success(result);
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

    /** 复制交底 */
    @RequirePermission("patent:disclosure:add")
    @PostMapping("/copy")
    public Result copy(@RequestBody Map<String, Object> body) {
        Long sourceId = body.get("sourceId") != null
                ? ((Number) body.get("sourceId")).longValue() : null;
        if (sourceId == null) {
            return Result.fail("sourceId 不能为空");
        }
        PatentDisclosure source = patentDisclosureService.getById(sourceId);
        if (source == null) {
            return Result.fail("源交底记录不存在");
        }
        source.setId(null);
        source.setTempNo(null);
        source.setInternalNo(null);
        source.setPatentStatus(null);
        source.setCopyFromId(sourceId);
        source.setCreateTime(null);
        source.setUpdateTime(null);
        source.setSyncedToPatent(0);
        source.setPatentApplicationId(null);
        patentDisclosureService.save(source);
        return Result.success(source, "复制成功");
    }

    /** 按主办人用户ID查询 */
    @RequirePermission("patent:disclosure:list")
    @GetMapping("/by-sponsor/{sponsorUserId}")
    public Result bySponsor(@PathVariable Long sponsorUserId) {
        List<PatentDisclosure> list = patentDisclosureService.list(
                new LambdaQueryWrapper<PatentDisclosure>()
                        .eq(PatentDisclosure::getSponsorUserId, sponsorUserId)
                        .orderByDesc(PatentDisclosure::getCreateTime));
        return Result.success(list);
    }

    /** 变更状态（写入状态日志） */
    @RequirePermission("patent:disclosure:edit")
    @PostMapping("/{id}/status")
    public Result changeStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PatentDisclosure disclosure = patentDisclosureService.getById(id);
        if (disclosure == null) {
            return Result.fail("交底记录不存在");
        }
        String fromStatus = disclosure.getPatentStatus();
        String toStatus = body.get("toStatus") != null ? body.get("toStatus").toString() : null;
        if (toStatus == null || toStatus.isBlank()) {
            return Result.fail("toStatus 不能为空");
        }
        disclosure.setPatentStatus(toStatus);
        patentDisclosureService.updateById(disclosure);

        DisclosureStatusLog log = new DisclosureStatusLog();
        log.setDisclosureId(id);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        Object opUserId = body.get("operatorUserId");
        if (opUserId != null) {
            log.setOperatorUserId(((Number) opUserId).longValue());
        }
        Object opName = body.get("operatorName");
        if (opName != null) {
            log.setOperatorName(opName.toString());
        }
        Object remark = body.get("remark");
        if (remark != null) {
            log.setRemark(remark.toString());
        }
        disclosureStatusLogService.save(log);
        return Result.success(disclosure, "状态变更成功");
    }

    // ═══════════════════════════════════════════════
    // 附件管理
    // ═══════════════════════════════════════════════

    /** 交底附件列表 */
    @RequirePermission("patent:disclosure:query")
    @GetMapping("/{id}/attachments")
    public Result listAttachments(@PathVariable Long id) {
        PatentDisclosure disclosure = patentDisclosureService.getById(id);
        if (disclosure == null) {
            return Result.fail("交底记录不存在");
        }
        List<DisclosureAttachment> attachments = disclosureAttachmentService.list(
                new LambdaQueryWrapper<DisclosureAttachment>()
                        .eq(DisclosureAttachment::getDisclosureId, id)
                        .eq(DisclosureAttachment::getDeleted, 0)
                        .orderByAsc(DisclosureAttachment::getSortNo));
        return Result.success(attachments);
    }

    /** 上传附件 */
    @RequirePermission("patent:disclosure:add")
    @PostMapping("/{id}/attachments")
    public Result uploadAttachment(@PathVariable Long id,
                                   @RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "bizType", defaultValue = "DISCLOSURE_OTHER") String bizType,
                                   @RequestParam(required = false) Long uploadUserId,
                                   @RequestParam(required = false) String uploadUserName) {
        PatentDisclosure disclosure = patentDisclosureService.getById(id);
        if (disclosure == null) {
            return Result.fail("交底记录不存在");
        }
        Result<String> uploadResult = uploadFileService.upload(file);
        if (uploadResult.getCode() != 200) {
            return Result.fail(uploadResult.getMessage());
        }
        String fileUrl = uploadResult.getData();
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }
        DisclosureAttachment attachment = new DisclosureAttachment();
        attachment.setDisclosureId(id);
        attachment.setInternalNo(disclosure.getInternalNo());
        attachment.setBizType(bizType);
        attachment.setFileName(originalFilename);
        attachment.setFileExt(ext);
        attachment.setFilePath(fileUrl);
        attachment.setFileUrl(fileUrl);
        attachment.setFileSize(file.getSize());
        attachment.setContentType(file.getContentType());
        attachment.setIsRequired("DISCLOSURE_DOC".equals(bizType) ? 1 : 0);
        attachment.setSortNo(0);
        attachment.setUploadUserId(uploadUserId);
        attachment.setUploadUserName(uploadUserName);
        attachment.setDeleted(0);
        disclosureAttachmentService.save(attachment);
        return Result.success(attachment, "附件上传成功");
    }

    /** 删除附件（逻辑删除） */
    @RequirePermission("patent:disclosure:delete")
    @DeleteMapping("/attachments/{attachmentId}")
    public Result deleteAttachment(@PathVariable Long attachmentId) {
        DisclosureAttachment attachment = disclosureAttachmentService.getById(attachmentId);
        if (attachment == null) {
            return Result.fail("附件不存在");
        }
        attachment.setDeleted(1);
        disclosureAttachmentService.updateById(attachment);
        return Result.successMsg("附件删除成功");
    }

    // ═══════════════════════════════════════════════
    // 申请包
    // ═══════════════════════════════════════════════

    /** 交底申请包列表 */
    @RequirePermission("patent:disclosure:query")
    @GetMapping("/{id}/packages")
    public Result listPackages(@PathVariable Long id) {
        PatentDisclosure disclosure = patentDisclosureService.getById(id);
        if (disclosure == null) {
            return Result.fail("交底记录不存在");
        }
        List<ApplicationPackage> packages = applicationPackageService.list(
                new LambdaQueryWrapper<ApplicationPackage>()
                        .eq(ApplicationPackage::getDisclosureId, id)
                        .orderByDesc(ApplicationPackage::getCreateTime));
        return Result.success(packages);
    }

    /** 上传申请包 */
    @RequirePermission("patent:disclosure:add")
    @PostMapping("/{id}/packages")
    public Result uploadPackage(@PathVariable Long id,
                                @RequestParam("file") MultipartFile file,
                                @RequestParam("packageType") String packageType,
                                @RequestParam(required = false) Long uploadUserId,
                                @RequestParam(required = false) String uploadUserName) {
        PatentDisclosure disclosure = patentDisclosureService.getById(id);
        if (disclosure == null) {
            return Result.fail("交底记录不存在");
        }
        Result<String> uploadResult = uploadFileService.upload(file);
        if (uploadResult.getCode() != 200) {
            return Result.fail(uploadResult.getMessage());
        }
        String fileUrl = uploadResult.getData();
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }
        ApplicationPackage pkg = new ApplicationPackage();
        pkg.setDisclosureId(id);
        pkg.setInternalNo(disclosure.getInternalNo());
        pkg.setPackageType(packageType);
        pkg.setFileName(originalFilename);
        pkg.setFileExt(ext);
        pkg.setFilePath(fileUrl);
        pkg.setFileUrl(fileUrl);
        pkg.setFileSize(file.getSize());
        pkg.setContentType(file.getContentType());
        pkg.setVersionNo(1);
        pkg.setIsCurrent(1);
        pkg.setUploadUserId(uploadUserId);
        pkg.setUploadUserName(uploadUserName);
        pkg.setUploadTime(new Date());
        pkg.setConfirmStatus("UNCONFIRMED");
        applicationPackageService.save(pkg);
        return Result.success(pkg, "申请包上传成功");
    }

    // ═══════════════════════════════════════════════
    // 状态日志
    // ═══════════════════════════════════════════════

    /** 交底状态变更日志 */
    @RequirePermission("patent:disclosure:query")
    @GetMapping("/{id}/status-logs")
    public Result statusLogs(@PathVariable Long id) {
        PatentDisclosure disclosure = patentDisclosureService.getById(id);
        if (disclosure == null) {
            return Result.fail("交底记录不存在");
        }
        List<DisclosureStatusLog> logs = disclosureStatusLogService.list(
                new LambdaQueryWrapper<DisclosureStatusLog>()
                        .eq(DisclosureStatusLog::getDisclosureId, id)
                        .orderByDesc(DisclosureStatusLog::getCreateTime));
        return Result.success(logs);
    }

    // ═══════════════════════════════════════════════
    // 费用
    // ═══════════════════════════════════════════════

    /** 交底关联费用列表 */
    @RequirePermission("patent:disclosure:query")
    @GetMapping("/{id}/fees")
    public Result fees(@PathVariable Long id) {
        PatentDisclosure disclosure = patentDisclosureService.getById(id);
        if (disclosure == null) {
            return Result.fail("交底记录不存在");
        }
        List<FeePayment> fees = feePaymentService.list(
                new LambdaQueryWrapper<FeePayment>()
                        .eq(FeePayment::getDisclosureId, id)
                        .orderByDesc(FeePayment::getCreateTime));
        return Result.success(fees);
    }

    // ═══════════════════════════════════════════════
    // 开票
    // ═══════════════════════════════════════════════

    /** 交底关联开票列表 */
    @RequirePermission("patent:disclosure:query")
    @GetMapping("/{id}/invoices")
    public Result invoices(@PathVariable Long id) {
        PatentDisclosure disclosure = patentDisclosureService.getById(id);
        if (disclosure == null) {
            return Result.fail("交底记录不存在");
        }
        List<Invoice> invoices = invoiceService.list(
                new LambdaQueryWrapper<Invoice>()
                        .eq(Invoice::getDisclosureId, id)
                        .orderByDesc(Invoice::getCreateTime));
        return Result.success(invoices);
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
