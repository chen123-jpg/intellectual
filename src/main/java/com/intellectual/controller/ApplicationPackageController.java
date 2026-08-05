package com.intellectual.controller;

import com.intellectual.annotation.RequirePermission;
import com.intellectual.annotation.RequirePermission.Logical;
import com.intellectual.model.dto.ApplicationPackageWorkflowDtos.DraftRequest;
import com.intellectual.model.dto.ApplicationPackageWorkflowDtos.RejectRequest;
import com.intellectual.model.dto.ApplicationPackageWorkflowDtos.SendRequest;
import com.intellectual.model.dto.ApplicationPackageWorkflowDtos.UnlockRequest;
import com.intellectual.model.dto.Result;
import com.intellectual.security.LoginUser;
import com.intellectual.service.ApplicationPackageWorkflowService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/** 申请包工作流接口。数据库自增 ID 不进入接口契约。 */
@RestController
@RequestMapping("/api/application-package")
public class ApplicationPackageController {

    private final ApplicationPackageWorkflowService workflowService;

    public ApplicationPackageController(ApplicationPackageWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * 申请包分页
     * @param pageNum
     * @param pageSize
     * @param status
     * @param internalNo
     * @param disclosureName
     * @param sponsorName
     * @return
     */
    @RequirePermission("patent:applicationPackage:list")
    @GetMapping("/batches")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) String internalNo,
                          @RequestParam(required = false) String disclosureName,
                          @RequestParam(required = false) String sponsorName) {
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        return Result.success(workflowService.list(safePageNum, safePageSize, status, internalNo,
                disclosureName, sponsorName, getLoginUser()));
    }

    /**
     * 申请包详情
     * @param packageToken
     * @return
     */
    @RequirePermission("patent:applicationPackage:query")
    @GetMapping("/batches/{packageToken}")
    public Result<?> detail(@PathVariable String packageToken) {
        return Result.success(workflowService.get(packageToken, getLoginUser()));
    }

    @RequirePermission("patent:applicationPackage:query")
    @GetMapping("/batches/by-disclosure/{disclosureId}")
    public Result<?> byDisclosure(@PathVariable Long disclosureId) {
        return Result.success(workflowService.getByDisclosure(disclosureId, getLoginUser()));
    }

    @RequirePermission("patent:applicationPackage:compose")
    @PostMapping("/drafts")
    public Result<?> createDraft(@RequestBody DraftRequest request) {
        Long disclosureId = request == null ? null : request.getDisclosureId();
        return Result.success(workflowService.createDraft(disclosureId, getLoginUser()), "申请包草稿已就绪");
    }

    @RequirePermission("patent:applicationPackage:compose")
    @PutMapping(value = "/batches/{packageToken}/files/{documentCode}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<?> uploadFile(@PathVariable String packageToken,
                                @PathVariable String documentCode,
                                @RequestPart("file") MultipartFile file) {
        return Result.success(workflowService.uploadFile(packageToken, documentCode, file, getLoginUser()),
                "申请文件上传成功");
    }

    @RequirePermission("patent:applicationPackage:compose")
    @DeleteMapping("/batches/{packageToken}/files/{documentCode}")
    public Result<?> removeFile(@PathVariable String packageToken,
                                @PathVariable String documentCode) {
        return Result.success(workflowService.removeFile(packageToken, documentCode, getLoginUser()),
                "申请文件已移除");
    }

    @RequirePermission("patent:applicationPackage:send")
    @PostMapping("/batches/{packageToken}/send")
    public Result<?> send(@PathVariable String packageToken, @RequestBody SendRequest request) {
        Long processUserId = request == null ? null : request.getProcessUserId();
        return Result.success(workflowService.send(packageToken, processUserId, getLoginUser()),
                "申请包发送成功");
    }

    @RequirePermission("patent:applicationPackage:receive")
    @PostMapping("/batches/{packageToken}/receive")
    public Result<?> receive(@PathVariable String packageToken) {
        return Result.success(workflowService.receive(packageToken, getLoginUser()), "申请包接收成功");
    }

    @RequirePermission("patent:applicationPackage:review")
    @PostMapping("/batches/{packageToken}/reject")
    public Result<?> reject(@PathVariable String packageToken, @RequestBody RejectRequest request) {
        return Result.success(workflowService.reject(packageToken, request, getLoginUser()), "申请包已退回");
    }

    @RequirePermission("patent:applicationPackage:review")
    @PostMapping("/batches/{packageToken}/approve")
    public Result<?> approve(@PathVariable String packageToken) {
        return Result.success(workflowService.approve(packageToken, getLoginUser()), "申请包审核通过并已锁定");
    }

    @RequirePermission("patent:applicationPackage:unlock")
    @PostMapping("/batches/{packageToken}/unlock")
    public Result<?> unlock(@PathVariable String packageToken, @RequestBody UnlockRequest request) {
        String reason = request == null ? null : request.getReason();
        return Result.success(workflowService.unlock(packageToken, reason, getLoginUser()), "申请包已解锁");
    }

    @RequirePermission("patent:applicationPackage:submit")
    @PostMapping(value = "/batches/{packageToken}/submit-cnipa",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<?> submitCnipa(@PathVariable String packageToken,
                                 @RequestParam String submissionNo,
                                 @RequestParam String submittedAt,
                                 @RequestPart("receipt") MultipartFile receipt) {
        return Result.success(workflowService.submitCnipa(packageToken, submissionNo,
                parseSubmittedAt(submittedAt), receipt, getLoginUser()), "国知局提交信息登记成功");
    }

    @RequirePermission(value = {
            "patent:applicationPackage:compose",
            "patent:applicationPackage:send"
    }, logical = Logical.OR)
    @GetMapping("/process-operators")
    public Result<?> processOperators() {
        return Result.success(workflowService.processOperators(getLoginUser()));
    }

    @RequirePermission(value = {
            "patent:applicationPackage:list",
            "patent:applicationPackage:query"
    }, logical = Logical.OR)
    @PostMapping("/files/{fileToken}/download-ticket")
    public Result<?> createDownloadTicket(@PathVariable String fileToken) {
        return Result.success(workflowService.createDownloadTicket(fileToken, getLoginUser()));
    }

    /** 下载票据与当前登录用户绑定，服务层读取后立即删除，确保只能使用一次。 */
    @GetMapping("/download/{ticket}")
    public ResponseEntity<Resource> download(@PathVariable String ticket) {
        return workflowService.download(ticket, getLoginUser());
    }

    private LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    private Date parseSubmittedAt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Date.from(Instant.parse(value.trim()));
        } catch (DateTimeParseException ignored) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("提交时间格式无效，请使用 ISO-8601 格式");
            }
        }
    }
}
