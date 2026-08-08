package com.intellectual.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellectual.annotation.RequirePermission;
import com.intellectual.exception.BusinessException;
import com.intellectual.model.dto.PatentDisclosureDTO;
import com.intellectual.model.dto.PatentDisclosureUpdateDTO;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.*;
import com.intellectual.model.vo.SponsorOptionVo;
import com.intellectual.security.LoginUser;
import com.intellectual.service.*;
import com.intellectual.service.impl.UploadFileServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import static com.intellectual.model.constants.TtableConstant.*;

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
    private SponsorDirectoryService sponsorDirectoryService;

    /** 新增或调整交底归属时可选择的启用主办人。不会暴露完整系统用户信息。 */
    @RequirePermission(value = {"patent:disclosure:add", "patent:disclosure:edit"},
            logical = RequirePermission.Logical.OR)
    @GetMapping("/sponsor-options")
    public Result sponsorOptions() {
        return Result.success(sponsorDirectoryService.listActiveSponsors());
    }

    /** 分页列表及高级搜索（全字段支持） */
    @RequirePermission("patent:disclosure:list")
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String disclosureName,
                       @RequestParam(required = false) String patentType,
                       @RequestParam(required = false) String patentStatus,
                       @RequestParam(required = false) String internalNo,
                       @RequestParam(required = false) String tempNo,
                       @RequestParam(required = false) String applicant,
                       @RequestParam(required = false) String inventor,
                       @RequestParam(required = false) String agent,
                       @RequestParam(required = false) String mentor,
                       @RequestParam(required = false) String businessPersonnel,
                       @RequestParam(required = false) String sponsor,
                       @RequestParam(required = false) Long sponsorUserId,
                       @RequestParam(required = false) String contactPerson,
                       @RequestParam(required = false) String manager,
                       @RequestParam(required = false) String requirement,
                       @RequestParam(required = false) String remark,
                       @RequestParam(required = false) String contactInfo,
                       @RequestParam(required = false) String contactEmail,
                       @RequestParam(required = false) String contactPhone,
                       @RequestParam(required = false) String entryUserName,
                       @RequestParam(required = false) Integer syncedToPatent,
                       @RequestParam(required = false) String disclosureDateStart,
                       @RequestParam(required = false) String disclosureDateEnd,
                       @RequestParam(required = false) String createTimeStart,
                       @RequestParam(required = false) String createTimeEnd) {
        LambdaQueryWrapper<PatentDisclosure> wrapper = new LambdaQueryWrapper<PatentDisclosure>()
                .like(disclosureName != null && !disclosureName.isBlank(), PatentDisclosure::getDisclosureName, disclosureName)
                .eq(patentType != null && !patentType.isBlank(), PatentDisclosure::getPatentType, patentType)
                .eq(patentStatus != null && !patentStatus.isBlank(), PatentDisclosure::getPatentStatus, patentStatus)
                .like(internalNo != null && !internalNo.isBlank(), PatentDisclosure::getInternalNo, internalNo)
                .like(tempNo != null && !tempNo.isBlank(), PatentDisclosure::getTempNo, tempNo)
                .like(applicant != null && !applicant.isBlank(), PatentDisclosure::getApplicant, applicant)
                .like(inventor != null && !inventor.isBlank(), PatentDisclosure::getInventor, inventor)
                .like(agent != null && !agent.isBlank(), PatentDisclosure::getAgent, agent)
                .like(mentor != null && !mentor.isBlank(), PatentDisclosure::getMentor, mentor)
                .like(businessPersonnel != null && !businessPersonnel.isBlank(),
                        PatentDisclosure::getBusinessPersonnel, businessPersonnel)
                .like(sponsor != null && !sponsor.isBlank(), PatentDisclosure::getSponsor, sponsor)
                .eq(sponsorUserId != null, PatentDisclosure::getSponsorUserId, sponsorUserId)
                .like(contactPerson != null && !contactPerson.isBlank(), PatentDisclosure::getContactPerson, contactPerson)
                .like(manager != null && !manager.isBlank(), PatentDisclosure::getManager, manager)
                .like(requirement != null && !requirement.isBlank(), PatentDisclosure::getRequirement, requirement)
                .like(remark != null && !remark.isBlank(), PatentDisclosure::getRemark, remark)
                .like(contactInfo != null && !contactInfo.isBlank(), PatentDisclosure::getContactInfo, contactInfo)
                .like(contactEmail != null && !contactEmail.isBlank(), PatentDisclosure::getContactEmail, contactEmail)
                .like(contactPhone != null && !contactPhone.isBlank(), PatentDisclosure::getContactPhone, contactPhone)
                .like(entryUserName != null && !entryUserName.isBlank(), PatentDisclosure::getEntryUserName, entryUserName)
                .eq(syncedToPatent != null, PatentDisclosure::getSyncedToPatent, syncedToPatent)
                .ge(disclosureDateStart != null && !disclosureDateStart.isBlank(), PatentDisclosure::getDisclosureDate, disclosureDateStart)
                .le(disclosureDateEnd != null && !disclosureDateEnd.isBlank(), PatentDisclosure::getDisclosureDate, disclosureDateEnd)
                .ge(createTimeStart != null && !createTimeStart.isBlank(), PatentDisclosure::getCreateTime, createTimeStart)
                .le(createTimeEnd != null && !createTimeEnd.isBlank(), PatentDisclosure::getCreateTime, createTimeEnd)
                .orderByDesc(PatentDisclosure::getCreateTime);
        applyDisclosureDataScope(wrapper);
        return pageResult(patentDisclosureService.list(wrapper), pageNum, pageSize);
    }

    /** 全部列表（不分页） */
    @RequirePermission("patent:disclosure:list")
    @GetMapping("/all")
    public Result all() {
        LambdaQueryWrapper<PatentDisclosure> wrapper =
                new LambdaQueryWrapper<PatentDisclosure>().orderByDesc(PatentDisclosure::getCreateTime);
        applyDisclosureDataScope(wrapper);
        return Result.success(patentDisclosureService.list(wrapper));
    }

    /** 详情 */
    @RequirePermission("patent:disclosure:query")
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        PatentDisclosure disclosure = getVisibleDisclosure(id);
        if (disclosure == null) {
            return Result.fail("交底记录不存在");
        }
        return Result.success(disclosure);
    }

    /** 详情（含关联附件、状态日志、费用和开票）。申请包通过独立工作流接口查询。 */
    @RequirePermission("patent:disclosure:query")
    @GetMapping("/{id}/detail")
    public Result detail(@PathVariable Long id) {
        PatentDisclosure disclosure = getVisibleDisclosure(id);
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
        Map<String, Object> result = new HashMap<>();
        result.put("disclosure", disclosure);
        result.put("attachments", attachments);
        result.put("statusLogs", statusLogs);
        result.put("fees", fees);
        result.put("invoices", invoices);
        return Result.success(result);
    }

    /** 新增 */
    @RequirePermission("patent:disclosure:add")
    @PostMapping(value = {"/add", "/with-attachments"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result add(
            @Valid @RequestPart("request") PatentDisclosureDTO request,
            @RequestPart("disclosureDocument") List<MultipartFile> disclosureDocuments,
            @RequestPart(value = "otherAttachments", required = false) List<MultipartFile> otherAttachments,
            @RequestParam(value = "sourceId", required = false) Long sourceId) {
        normalizeSponsor(request);
        if (sourceId != null && getVisibleDisclosure(sourceId) == null) {
            return Result.fail("复制来源交底记录不存在");
        }

        LoginUser loginUser = getLoginUser();
        PatentDisclosure disclosure = patentDisclosureService.createWithAttachments(
                request,
                disclosureDocuments,
                otherAttachments,
                sourceId,
                loginUser != null ? loginUser.getUserId() : null,
                loginUser != null ? loginUser.getLoginName() : null);
        return Result.success(disclosure, "交底信息及附件创建成功");
    }

    /** 修改 */
    @RequirePermission("patent:disclosure:edit")
    @PutMapping
    public Result update(@Valid @RequestBody PatentDisclosureUpdateDTO request) {
        if (request.getId() == null) {
            return Result.fail("ID不能为空");
        }
        PatentDisclosure existing = getVisibleDisclosure(request.getId());
        if (existing == null) {
            return Result.fail("交底记录不存在");
        }
        // 定稿待报/已申报只能由申请包工作流变更，普通编辑接口不允许直接设置，
        // 避免绕过申请包流程直接改动影响后续业务流程。
        if (request.getPatentStatus() != null
                && !request.getPatentStatus().equals(existing.getPatentStatus())
                && (DISCLOSURE_STATUS_PENDING_REPORT.equals(request.getPatentStatus())
                || DISCLOSURE_STATUS_REPORTED.equals(request.getPatentStatus()))) {
            return Result.fail("定稿待报和已申报只能由申请包工作流变更");
        }
        LoginUser loginUser = getLoginUser();
        if (loginUser != null && hasRole(loginUser, ROLE_ORGANIZER)
                && !hasRole(loginUser, ROLE_ADMIN)) {
            request.setSponsorUserId(existing.getSponsorUserId());
            request.setSponsor(existing.getSponsor());
        } else if (request.getSponsorUserId() == null && existing.getSponsorUserId() != null) {
            request.setSponsorUserId(existing.getSponsorUserId());
            request.setSponsor(existing.getSponsor());
        } else {
            normalizeSponsor(request);
        }
        PatentDisclosure disclosure = toDisclosure(request, false);
        if (!patentDisclosureService.updateWithRelatedRecords(disclosure)) {
            return Result.fail("交底记录不存在");
        }
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
    @RequirePermission(value = {"patent:disclosure:copy", "patent:disclosure:add"},
            logical = RequirePermission.Logical.OR)
    @PostMapping("/copy")
    public Result copy(@RequestBody Map<String, Object> body) {
        Long sourceId = body.get("sourceId") != null
                ? ((Number) body.get("sourceId")).longValue() : null;
        if (sourceId == null) {
            return Result.fail("sourceId 不能为空");
        }
        PatentDisclosure source = getVisibleDisclosure(sourceId);
        if (source == null) {
            return Result.fail("源交底记录不存在");
        }
        PatentDisclosureDTO copy = new PatentDisclosureDTO();
        BeanUtils.copyProperties(source, copy);
        copy.setId(null);
        copy.setInternalNo(null);
        copy.setPatentStatus(null);
        return Result.success(copy, "已读取历史交底，请补充附件后保存");
    }

    /** 按主办人用户ID查询 */
    @RequirePermission("patent:disclosure:list")
    @GetMapping("/by-sponsor/{sponsorUserId}")
    public Result bySponsor(@PathVariable Long sponsorUserId) {
        LambdaQueryWrapper<PatentDisclosure> wrapper = new LambdaQueryWrapper<PatentDisclosure>()
                .eq(PatentDisclosure::getSponsorUserId, sponsorUserId)
                .orderByDesc(PatentDisclosure::getCreateTime);
        applyDisclosureDataScope(wrapper);
        List<PatentDisclosure> list = patentDisclosureService.list(wrapper);
        return Result.success(list);
    }

    /** 变更状态（写入状态日志） */
    @RequirePermission("patent:disclosure:edit")
    @PostMapping("/{id}/status")
    public Result changeStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PatentDisclosure disclosure = getVisibleDisclosure(id);
        if (disclosure == null) {
            return Result.fail("交底记录不存在");
        }
        String fromStatus = disclosure.getPatentStatus();
        String toStatus = body.get("toStatus") != null ? body.get("toStatus").toString() : null;
        if (toStatus == null || toStatus.isBlank()) {
            return Result.fail("toStatus 不能为空");
        }
        if (DISCLOSURE_STATUS_PENDING_REPORT.equals(toStatus)
                || DISCLOSURE_STATUS_REPORTED.equals(toStatus)) {
            return Result.fail("定稿待报和已申报只能由申请包工作流变更");
        }
        disclosure.setPatentStatus(toStatus);
        patentDisclosureService.updateById(disclosure);

        DisclosureStatusLog log = new DisclosureStatusLog();
        log.setDisclosureId(id);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        LoginUser loginUser = getLoginUser();
        if (loginUser != null) {
            log.setOperatorUserId(loginUser.getUserId());
            log.setOperatorName(loginUser.getLoginName());
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
    @RequirePermission(value = {"patent:disclosure:query", "patent:disclosure:attachment:upload",
            "patent:disclosure:add"},
            logical = RequirePermission.Logical.OR)
    @GetMapping("/{id}/attachments")
    public Result listAttachments(@PathVariable Long id) {
        PatentDisclosure disclosure = getVisibleDisclosure(id);
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
    @RequirePermission(value = {"patent:disclosure:attachment:upload", "patent:disclosure:add"},
            logical = RequirePermission.Logical.OR)
    @PostMapping("/{id}/attachments")
    public Result uploadAttachment(@PathVariable Long id,
                                   @RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "bizType", defaultValue = "DISCLOSURE_OTHER") String bizType) {
        PatentDisclosure disclosure = getVisibleDisclosure(id);
        if (disclosure == null) {
            return Result.fail("交底记录不存在");
        }
        if (!DISCLOSURE_ATTACHMENT_TYPES.contains(bizType)) {
            return Result.fail("不支持的附件业务类型");
        }
        if (DISCLOSURE_DOC.equals(bizType)) {
            if (!isWordDocument(file)) {
                return Result.fail("交底书只能上传 .doc 或 .docx 格式的 Word 文档");
            }
            long documentCount = disclosureAttachmentService.count(
                    new LambdaQueryWrapper<DisclosureAttachment>()
                            .eq(DisclosureAttachment::getDisclosureId, id)
                            .eq(DisclosureAttachment::getBizType, DISCLOSURE_DOC)
                            .eq(DisclosureAttachment::getDeleted, 0));
            if (documentCount > 0) {
                return Result.fail("交底书只能保留一份，请先删除原交底书");
            }
        }

        DisclosureAttachment attachment = storeAttachment(file, bizType);
        attachment.setDisclosureId(id);
        attachment.setInternalNo(disclosure.getInternalNo());
        disclosureAttachmentService.save(attachment);
        return Result.success(attachment, "附件上传成功");
    }

    /** 更换交底书：新文件保存成功后再逻辑删除旧交底书。 */
    @RequirePermission(value = {"patent:disclosure:attachment:upload", "patent:disclosure:add"},
            logical = RequirePermission.Logical.OR)
    @PutMapping(value = "/{id}/attachments/disclosure-document",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result replaceDisclosureDocument(@PathVariable Long id,
                                            @RequestPart("file") MultipartFile file) {
        PatentDisclosure disclosure = getVisibleDisclosure(id);
        if (disclosure == null) {
            return Result.fail("交底记录不存在");
        }
        LoginUser loginUser = getLoginUser();
        DisclosureAttachment replacement = patentDisclosureService.replaceDisclosureDocument(
                id,
                disclosure.getInternalNo(),
                file,
                loginUser != null ? loginUser.getUserId() : null,
                loginUser != null ? loginUser.getLoginName() : null);
        return Result.success(replacement, "交底书更换成功");
    }

    /** 删除附件（逻辑删除） */
    @RequirePermission(value = {"patent:disclosure:delete", "patent:disclosure:attachment:upload",
            "patent:disclosure:add"},
            logical = RequirePermission.Logical.OR)
    @DeleteMapping("/attachments/{attachmentId}")
    public Result deleteAttachment(@PathVariable Long attachmentId) {
        DisclosureAttachment attachment = disclosureAttachmentService.getById(attachmentId);
        if (attachment == null) {
            return Result.fail("附件不存在");
        }
        if (getVisibleDisclosure(attachment.getDisclosureId()) == null) {
            return Result.fail("附件不存在");
        }
        attachment.setDeleted(1);
        disclosureAttachmentService.updateById(attachment);
        return Result.successMsg("附件删除成功");
    }

    // ═══════════════════════════════════════════════
    // 状态日志
    // ═══════════════════════════════════════════════

    /** 交底状态变更日志 */
    @RequirePermission("patent:disclosure:query")
    @GetMapping("/{id}/status-logs")
    public Result statusLogs(@PathVariable Long id) {
        PatentDisclosure disclosure = getVisibleDisclosure(id);
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
        PatentDisclosure disclosure = getVisibleDisclosure(id);
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
        PatentDisclosure disclosure = getVisibleDisclosure(id);
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

    private Result pageResult(List<PatentDisclosure> all, int pageNum, int pageSize) {
        int total = all.size();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<PatentDisclosure> page = from < total ? all.subList(from, to) : List.of();

        Map<Long, List<DisclosureAttachment>> attachmentsByDisclosureId = new HashMap<>();
        if (!page.isEmpty()) {
            List<Long> disclosureIds = page.stream()
                    .map(PatentDisclosure::getId)
                    .filter(Objects::nonNull)
                    .toList();
            if (!disclosureIds.isEmpty()) {
                List<DisclosureAttachment> attachments = disclosureAttachmentService.list(
                        new LambdaQueryWrapper<DisclosureAttachment>()
                                .in(DisclosureAttachment::getDisclosureId, disclosureIds)
                                .eq(DisclosureAttachment::getDeleted, 0)
                                .orderByAsc(DisclosureAttachment::getDisclosureId)
                                .orderByAsc(DisclosureAttachment::getSortNo));
                for (DisclosureAttachment attachment : attachments) {
                    attachmentsByDisclosureId
                            .computeIfAbsent(attachment.getDisclosureId(), key -> new ArrayList<>())
                            .add(attachment);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", page);
        result.put("attachmentsByDisclosureId", attachmentsByDisclosureId);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    private PatentDisclosure toDisclosure(PatentDisclosureDTO request, boolean ignoreId) {
        PatentDisclosure disclosure = new PatentDisclosure();
        if (ignoreId) {
            BeanUtils.copyProperties(request, disclosure, "id");
        } else {
            BeanUtils.copyProperties(request, disclosure);
        }
        return disclosure;
    }

    private DisclosureAttachment storeAttachment(MultipartFile file, String bizType) {
        Result<String> uploadResult = uploadFileService.upload(file);
        if (uploadResult.getCode() != 200) {
            throw new BusinessException(uploadResult.getMessage());
        }

        LoginUser loginUser = getLoginUser();
        DisclosureAttachment attachment = new DisclosureAttachment();
        attachment.setBizType(bizType);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileExt(getFileExtension(file));
        attachment.setFilePath(uploadResult.getData());
        attachment.setFileUrl(uploadResult.getData());
        attachment.setFileSize(file.getSize());
        attachment.setContentType(file.getContentType());
        attachment.setIsRequired(DISCLOSURE_DOC.equals(bizType) ? 1 : 0);
        attachment.setSortNo(0);
        if (loginUser != null) {
            attachment.setUploadUserId(loginUser.getUserId());
            attachment.setUploadUserName(loginUser.getLoginName());
        }
        attachment.setDeleted(0);
        return attachment;
    }

    private boolean isWordDocument(MultipartFile file) {
        return file != null && !file.isEmpty() && WORD_EXTENSIONS.contains(getFileExtension(file));
    }

    private String getFileExtension(MultipartFile file) {
        String filename = file != null ? file.getOriginalFilename() : null;
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    /** 立项人员只看自己录入的交底，主办人只看分配给自己的交底；管理员保持全量数据权限。 */
    private void applyDisclosureDataScope(LambdaQueryWrapper<PatentDisclosure> wrapper) {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null || hasRole(loginUser, ROLE_ADMIN)) {
            return;
        }
        if (hasRole(loginUser, ROLE_ORGANIZER)) {
            wrapper.eq(PatentDisclosure::getSponsorUserId, loginUser.getUserId());
        } else if (hasRole(loginUser, ROLE_PROJECT_INITIATOR)) {
            wrapper.eq(PatentDisclosure::getEntryUserId, loginUser.getUserId());
        }
    }

    /** 对单条数据应用与列表一致的数据范围，防止后续误配 query 权限造成越权。 */
    private PatentDisclosure getVisibleDisclosure(Long id) {
        PatentDisclosure disclosure = patentDisclosureService.getById(id);
        LoginUser loginUser = getLoginUser();
        if (disclosure == null || loginUser == null || hasRole(loginUser, ROLE_ADMIN)) {
            return disclosure;
        }
        if (hasRole(loginUser, ROLE_ORGANIZER)
                && !Objects.equals(disclosure.getSponsorUserId(), loginUser.getUserId())) {
            return null;
        }
        if (!hasRole(loginUser, ROLE_ORGANIZER)
                && hasRole(loginUser, ROLE_PROJECT_INITIATOR)
                && !Objects.equals(disclosure.getEntryUserId(), loginUser.getUserId())) {
            return null;
        }
        return disclosure;
    }

    private void normalizeSponsor(PatentDisclosureDTO request) {
        SponsorOptionVo sponsor = sponsorDirectoryService.requireActiveSponsor(request.getSponsorUserId());
        request.setSponsorUserId(sponsor.getUserId());
        request.setSponsor(sponsor.getUserName());
    }

    private boolean hasRole(LoginUser loginUser, String role) {
        return loginUser.getRoles() != null && loginUser.getRoles().contains(role);
    }

    private LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }
}
