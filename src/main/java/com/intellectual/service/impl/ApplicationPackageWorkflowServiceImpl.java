package com.intellectual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellectual.event.ApplicationPackageMailEvent;
import com.intellectual.exception.BusinessException;
import com.intellectual.model.dto.ApplicationPackageWorkflowDtos.*;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.*;
import com.intellectual.redis.RedisUtils;
import com.intellectual.security.LoginUser;
import com.intellectual.service.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.intellectual.model.constants.TtableConstant.*;

@Service
public class ApplicationPackageWorkflowServiceImpl implements ApplicationPackageWorkflowService {
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final long DOWNLOAD_TICKET_SECONDS = 300L;
    private static final String DOWNLOAD_KEY_PREFIX = "application-package:download:";

    private final ApplicationPackageBatchService batchService;
    private final ApplicationPackageService fileService;
    private final ApplicationPackageReviewIssueService issueService;
    private final ApplicationPackageActionLogService actionLogService;
    private final PatentDisclosureService disclosureService;
    private final PatentNewApplicationService newApplicationService;
    private final UserService userService;
    private final UserRoleService userRoleService;
    private final RoleService roleService;
    private final UploadFileServiceImpl uploadFileService;
    private final RedisUtils redisUtils;
    private final ApplicationEventPublisher eventPublisher;

    public ApplicationPackageWorkflowServiceImpl(
            ApplicationPackageBatchService batchService,
            ApplicationPackageService fileService,
            ApplicationPackageReviewIssueService issueService,
            ApplicationPackageActionLogService actionLogService,
            PatentDisclosureService disclosureService,
            PatentNewApplicationService newApplicationService,
            UserService userService,
            UserRoleService userRoleService,
            RoleService roleService,
            UploadFileServiceImpl uploadFileService,
            RedisUtils redisUtils,
            ApplicationEventPublisher eventPublisher) {
        this.batchService = batchService;
        this.fileService = fileService;
        this.issueService = issueService;
        this.actionLogService = actionLogService;
        this.disclosureService = disclosureService;
        this.newApplicationService = newApplicationService;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.roleService = roleService;
        this.uploadFileService = uploadFileService;
        this.redisUtils = redisUtils;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 展示与人员有关的申请包
     * @param pageNum
     * @param pageSize
     * @param status
     * @param internalNo
     * @param disclosureName
     * @param sponsorName
     * @param actor
     * @return
     */
    @Override
    public Map<String, Object> list(int pageNum, int pageSize, String status, String internalNo,
                                    String disclosureName, String sponsorName, LoginUser actor) {
        requireAuthenticated(actor);
        LambdaQueryWrapper<ApplicationPackageBatch> wrapper = new LambdaQueryWrapper<ApplicationPackageBatch>()
                .eq(hasText(status), ApplicationPackageBatch::getStatus, status)
                .like(hasText(internalNo), ApplicationPackageBatch::getInternalNo, internalNo)
                .like(hasText(disclosureName), ApplicationPackageBatch::getDisclosureName, disclosureName)
                .like(hasText(sponsorName), ApplicationPackageBatch::getSponsorUserName, sponsorName)
                .orderByDesc(ApplicationPackageBatch::getUpdateTime);
        applyDataScope(wrapper, actor);
        Page<ApplicationPackageBatch> page = batchService.page(new Page<>(pageNum, pageSize), wrapper);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", page.getRecords().stream().map(batch -> toView(batch, false)).toList());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return result;
    }

    @Override
    public BatchView get(String packageToken, LoginUser actor) {
        return toView(requireVisibleBatch(packageToken, actor), true);
    }

    /**
     * 根据交底记录获取申请包
     * @param disclosureId
     * @param actor
     * @return
     */
    @Override
    public BatchView getByDisclosure(Long disclosureId, LoginUser actor) {
        requireAuthenticated(actor);
        if (disclosureId == null) {
            return null;
        }
        ApplicationPackageBatch batch = batchService.getOne(
                new LambdaQueryWrapper<ApplicationPackageBatch>()
                        .eq(ApplicationPackageBatch::getDisclosureId, disclosureId), false);
        if (batch == null) {
            return null;
        }
        if (!canView(batch, actor)) {
            throw invisible();
        }
        return toView(batch, true);
    }

    /**
     * 创建草稿
     * @param disclosureId
     * @param actor
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchView createDraft(Long disclosureId, LoginUser actor) {
        requireAuthenticated(actor);
        if (disclosureId == null) {
            throw new BusinessException("交底ID不能为空");
        }
        PatentDisclosure disclosure = disclosureService.getById(disclosureId);
        if (disclosure == null) {
            throw new BusinessException("交底记录不存在");
        }
        requireOrganizerOwner(disclosure, actor);
        ApplicationPackageBatch existing = batchService.getOne(
                new LambdaQueryWrapper<ApplicationPackageBatch>()
                        .eq(ApplicationPackageBatch::getDisclosureId, disclosureId), false);
        if (existing != null) {
            if (!canView(existing, actor)) {
                throw invisible();
            }
            return toView(existing, true);
        }
        if (!DISCLOSURE_STATUS_FINAL.equals(disclosure.getPatentStatus())) {
            throw new BusinessException("只有定稿状态的交底才能组建申请包");
        }
        if (disclosure.getSponsorUserId() == null) {
            throw new BusinessException("交底记录未绑定主办人员");
        }
        ApplicationPackageBatch batch = new ApplicationPackageBatch();
        batch.setPublicId(UUID.randomUUID().toString());
        batch.setDisclosureId(disclosure.getId());
        batch.setInternalNo(disclosure.getInternalNo());
        batch.setDisclosureName(disclosure.getDisclosureName());
        batch.setSponsorUserId(disclosure.getSponsorUserId());
        batch.setSponsorUserName(displayName(disclosure.getSponsor(), actor.getLoginName()));
        batch.setStatus(PACKAGE_STATUS_DRAFT);
        batch.setRoundNo(1);
        batch.setLockVersion(0);
        if (!batchService.save(batch)) {
            throw new BusinessException("申请包草稿创建失败");
        }
        createAction(batch, "CREATE_DRAFT", null, PACKAGE_STATUS_DRAFT,
                null, "创建申请包草稿", actor, null, null, null);
        return toView(batch, true);
    }

    /**
     * 上传替换申请包内容
     * @param packageToken
     * @param documentCode
     * @param file
     * @param actor
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchView uploadFile(String packageToken, String documentCode,
                                MultipartFile file, LoginUser actor) {
        ApplicationPackageBatch batch = requireVisibleBatch(packageToken, actor);
        requireSponsorOwner(batch, actor);
        if (!Set.of(PACKAGE_STATUS_DRAFT, PACKAGE_STATUS_REJECTED).contains(batch.getStatus())) {
            throw new BusinessException("当前状态不允许修改申请包文件");
        }
        if (!PACKAGE_DOCUMENT_CODES.contains(documentCode)) {
            throw new BusinessException("不支持的申请文件类型");
        }
        byte[] bytes = readAndValidate(file, documentCode);
        String fileUrl = upload(file);
        registerFileRollback(fileUrl);

        ApplicationPackage old = fileService.getOne(
                new LambdaQueryWrapper<ApplicationPackage>()
                        .eq(ApplicationPackage::getBatchId, batch.getId())
                        .eq(ApplicationPackage::getFileRole, PACKAGE_FILE_ROLE_DOCUMENT)
                        .eq(ApplicationPackage::getDocumentCode, documentCode)
                        .eq(ApplicationPackage::getIsCurrent, 1), false);
        int version;
        if (old != null && old.getVersionNo() != null) {
            version = old.getVersionNo() + 1;
        } else {
            ApplicationPackage latest = fileService.getOne(
                    new LambdaQueryWrapper<ApplicationPackage>()
                            .eq(ApplicationPackage::getBatchId, batch.getId())
                            .eq(ApplicationPackage::getFileRole, PACKAGE_FILE_ROLE_DOCUMENT)
                            .eq(ApplicationPackage::getDocumentCode, documentCode)
                            .orderByDesc(ApplicationPackage::getVersionNo)
                            .last("LIMIT 1"), false);
            version = latest == null || latest.getVersionNo() == null ? 1 : latest.getVersionNo() + 1;
        }
        if (old != null) {
            fileService.update(new LambdaUpdateWrapper<ApplicationPackage>()
                    .eq(ApplicationPackage::getId, old.getId())
                    .set(ApplicationPackage::getIsCurrent, 0));
        }

        ApplicationPackage entity = buildFile(batch, documentCode, PACKAGE_FILE_ROLE_DOCUMENT,
                file, bytes, fileUrl, version, actor);
        if (!fileService.save(entity)) {
            throw new BusinessException("申请文件保存失败");
        }
        createAction(batch, old == null ? "UPLOAD_FILE" : "REPLACE_FILE",
                batch.getStatus(), batch.getStatus(), documentCode,
                (old == null ? "上传" : "替换") + documentLabel(documentCode),
                actor, null, null, null);
        return toView(batch, true);
    }

    /**
     * 移除文件
     * @param packageToken
     * @param documentCode
     * @param actor
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchView removeFile(String packageToken, String documentCode, LoginUser actor) {
        ApplicationPackageBatch batch = requireVisibleBatch(packageToken, actor);
        requireSponsorOwner(batch, actor);
        if (!Set.of(PACKAGE_STATUS_DRAFT, PACKAGE_STATUS_REJECTED).contains(batch.getStatus())) {
            throw new BusinessException("当前状态不允许移除申请包文件");
        }
        if (!PACKAGE_DOCUMENT_CODES.contains(documentCode)) {
            throw new BusinessException("不支持的申请文件类型");
        }
        ApplicationPackage current = fileService.getOne(
                new LambdaQueryWrapper<ApplicationPackage>()
                        .eq(ApplicationPackage::getBatchId, batch.getId())
                        .eq(ApplicationPackage::getFileRole, PACKAGE_FILE_ROLE_DOCUMENT)
                        .eq(ApplicationPackage::getDocumentCode, documentCode)
                        .eq(ApplicationPackage::getIsCurrent, 1), false);
        if (current == null) {
            throw new BusinessException("当前文件不存在或已被移除");
        }
        boolean updated = fileService.update(new LambdaUpdateWrapper<ApplicationPackage>()
                .eq(ApplicationPackage::getId, current.getId())
                .eq(ApplicationPackage::getIsCurrent, 1)
                .set(ApplicationPackage::getIsCurrent, 0)
                .set(ApplicationPackage::getUpdateTime, new Date()));
        if (!updated) {
            throw new BusinessException("文件已被其他操作更新，请刷新后重试");
        }
        createAction(batch, "REMOVE_FILE", batch.getStatus(), batch.getStatus(), documentCode,
                "移除" + documentLabel(documentCode) + "当前版本", actor, null, null, null);
        return toView(batch, true);
    }

    /**
     * 发送申请包
     * @param packageToken
     * @param processUserId
     * @param actor
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchView send(String packageToken, Long processUserId, LoginUser actor) {
        ApplicationPackageBatch batch = requireVisibleBatch(packageToken, actor);
        requireSponsorOwner(batch, actor);
        String from = batch.getStatus();
        if (!Set.of(PACKAGE_STATUS_DRAFT, PACKAGE_STATUS_REJECTED).contains(from)) {
            throw new BusinessException("当前状态不允许发送申请包");
        }
        validateCompletePackage(batch);
        User processUser;
        if (PACKAGE_STATUS_DRAFT.equals(from)) {
            processUser = requireActiveProcessOperator(processUserId);
            batch.setProcessUserId(processUser.getUserId());
            batch.setProcessUserName(displayName(processUser.getUserName(), processUser.getLoginName()));
        } else {
            processUser = requireActiveProcessOperator(batch.getProcessUserId());
            batch.setRoundNo(defaultInt(batch.getRoundNo(), 1) + 1);
        }
        batch.setStatus(PACKAGE_STATUS_PENDING_RECEIVE);
        batch.setSentAt(new Date());
        batch.setReceivedAt(null);
        batch.setRejectReason(null);
        updateBatch(batch);
        String action = PACKAGE_STATUS_DRAFT.equals(from) ? "SEND" : "RESEND";
        createAction(batch, action, from, PACKAGE_STATUS_PENDING_RECEIVE, null,
                action.equals("SEND") ? "主办人发送申请包" : "主办人重新发送申请包",
                actor, processUser.getEmail(),
                "申请包待接收：" + safe(batch.getDisclosureName()),
                "<p>主办人已发送申请包，请登录系统接收并审核。</p><p>内部编号："
                        + safe(batch.getInternalNo()) + "</p>");
        return toView(batch, true);
    }

    /**
     * 接收申请包
     * @param packageToken
     * @param actor
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchView receive(String packageToken, LoginUser actor) {
        ApplicationPackageBatch batch = requireVisibleBatch(packageToken, actor);
        requireProcessOwner(batch, actor);
        requireStatus(batch, PACKAGE_STATUS_PENDING_RECEIVE);
        batch.setStatus(PACKAGE_STATUS_REVIEWING);
        batch.setReceivedAt(new Date());
        updateBatch(batch);
        createAction(batch, "RECEIVE", PACKAGE_STATUS_PENDING_RECEIVE, PACKAGE_STATUS_REVIEWING,
                null, "流程专员接收申请包", actor, null, null, null);
        return toView(batch, true);
    }

    /**
     * 拒绝申请包
     * @param packageToken
     * @param request
     * @param actor
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchView reject(String packageToken, RejectRequest request, LoginUser actor) {
        ApplicationPackageBatch batch = requireVisibleBatch(packageToken, actor);
        requireProcessOwner(batch, actor);
        requireStatus(batch, PACKAGE_STATUS_REVIEWING);
        if (request == null || !hasText(request.getReason())) {
            throw new BusinessException("退回总原因不能为空");
        }
        ApplicationPackageReviewIssue overallIssue = new ApplicationPackageReviewIssue();
        overallIssue.setBatchId(batch.getId());
        overallIssue.setRoundNo(batch.getRoundNo());
        overallIssue.setDocumentCode(null);
        overallIssue.setIssueText(request.getReason().trim());
        overallIssue.setReviewerUserId(actor.getUserId());
        overallIssue.setReviewerUserName(actor.getLoginName());
        overallIssue.setCreateTime(new Date());
        issueService.save(overallIssue);
        if (request.getIssues() != null) {
            for (IssueRequest item : request.getIssues()) {
                if (item == null || !hasText(item.getIssueText())) {
                    continue;
                }
                if (hasText(item.getDocumentCode()) && !PACKAGE_DOCUMENT_CODES.contains(item.getDocumentCode())) {
                    throw new BusinessException("退回意见包含未知文件类型");
                }
                ApplicationPackageReviewIssue issue = new ApplicationPackageReviewIssue();
                issue.setBatchId(batch.getId());
                issue.setRoundNo(batch.getRoundNo());
                issue.setDocumentCode(item.getDocumentCode());
                issue.setIssueText(item.getIssueText().trim());
                issue.setReviewerUserId(actor.getUserId());
                issue.setReviewerUserName(actor.getLoginName());
                issue.setCreateTime(new Date());
                issueService.save(issue);
            }
        }
        batch.setStatus(PACKAGE_STATUS_REJECTED);
        batch.setRejectedAt(new Date());
        batch.setRejectReason(request.getReason().trim());
        updateBatch(batch);
        User sponsor = userService.getById(batch.getSponsorUserId());
        String issueSummary = request.getIssues() == null ? "" : request.getIssues().stream()
                .filter(Objects::nonNull)
                .filter(item -> hasText(item.getIssueText()))
                .map(item -> safe(documentLabel(item.getDocumentCode())) + "：" + safe(item.getIssueText()))
                .collect(Collectors.joining("<br/>"));
        createAction(batch, "REJECT", PACKAGE_STATUS_REVIEWING, PACKAGE_STATUS_REJECTED,
                null, request.getReason().trim(), actor, sponsor == null ? null : sponsor.getEmail(),
                "申请包已退回：" + safe(batch.getDisclosureName()),
                "<p>退回原因：" + safe(request.getReason()) + "</p>"
                        + (issueSummary.isBlank() ? "" : "<p>文件问题：<br/>" + issueSummary + "</p>"));
        return toView(batch, true);
    }

    /**
     * 接收申请包
     * @param packageToken
     * @param actor
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchView approve(String packageToken, LoginUser actor) {
        ApplicationPackageBatch batch = requireVisibleBatch(packageToken, actor);
        requireProcessOwner(batch, actor);
        requireStatus(batch, PACKAGE_STATUS_REVIEWING);
        validateCompletePackage(batch);
        PatentDisclosure disclosure = requireDisclosure(batch);
        if (!DISCLOSURE_STATUS_FINAL.equals(disclosure.getPatentStatus())) {
            throw new BusinessException("只有定稿状态的交底申请包可以审核通过");
        }
        batch.setStatus(PACKAGE_STATUS_APPROVED);
        batch.setApprovedUserId(actor.getUserId());
        batch.setApprovedUserName(actor.getLoginName());
        batch.setApprovedAt(new Date());
        updateBatch(batch);
        disclosure.setPatentStatus(DISCLOSURE_STATUS_PENDING_REPORT);
        disclosure.setPendingReportAt(new Date());
        if (!disclosureService.updateById(disclosure)) {
            throw new BusinessException("交底状态更新失败");
        }
        User sponsor = userService.getById(batch.getSponsorUserId());
        createAction(batch, "APPROVE", PACKAGE_STATUS_REVIEWING, PACKAGE_STATUS_APPROVED,
                null, "申请包审核通过并锁定", actor, sponsor == null ? null : sponsor.getEmail(),
                "申请包审核通过：" + safe(batch.getDisclosureName()),
                "<p>申请包已审核通过，交底状态已变更为定稿待报。</p>");
        return toView(batch, true);
    }

    /**
     * 管理员解锁
     * @param packageToken
     * @param reason
     * @param actor
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchView unlock(String packageToken, String reason, LoginUser actor) {
        ApplicationPackageBatch batch = requireVisibleBatch(packageToken, actor);
        if (!isAdmin(actor)) {
            throw new BusinessException("只有管理员可以解锁申请包");
        }
        requireStatus(batch, PACKAGE_STATUS_APPROVED);
        if (!hasText(reason)) {
            throw new BusinessException("解锁原因不能为空");
        }
        PatentDisclosure disclosure = requireDisclosure(batch);
        Date unlockedAt = new Date();
        int currentVersion = defaultInt(batch.getLockVersion(), 0);
        boolean unlocked = batchService.update(new LambdaUpdateWrapper<ApplicationPackageBatch>()
                .eq(ApplicationPackageBatch::getId, batch.getId())
                .eq(ApplicationPackageBatch::getStatus, PACKAGE_STATUS_APPROVED)
                .eq(ApplicationPackageBatch::getLockVersion, currentVersion)
                .set(ApplicationPackageBatch::getStatus, PACKAGE_STATUS_REVIEWING)
                .set(ApplicationPackageBatch::getApprovedUserId, null)
                .set(ApplicationPackageBatch::getApprovedUserName, null)
                .set(ApplicationPackageBatch::getApprovedAt, null)
                .set(ApplicationPackageBatch::getUnlockedUserId, actor.getUserId())
                .set(ApplicationPackageBatch::getUnlockedUserName, actor.getLoginName())
                .set(ApplicationPackageBatch::getUnlockedAt, unlockedAt)
                .set(ApplicationPackageBatch::getLockVersion, currentVersion + 1));
        if (!unlocked) {
            throw new BusinessException("申请包已被其他操作更新，请刷新后重试");
        }
        batch.setStatus(PACKAGE_STATUS_REVIEWING);
        batch.setApprovedUserId(null);
        batch.setApprovedUserName(null);
        batch.setApprovedAt(null);
        batch.setUnlockedUserId(actor.getUserId());
        batch.setUnlockedUserName(actor.getLoginName());
        batch.setUnlockedAt(unlockedAt);
        batch.setLockVersion(currentVersion + 1);
        disclosure.setPatentStatus(DISCLOSURE_STATUS_FINAL);
        disclosure.setPendingReportAt(null);
        if (!disclosureService.updateById(disclosure)) {
            throw new BusinessException("交底状态恢复失败");
        }
        User sponsor = userService.getById(batch.getSponsorUserId());
        User process = userService.getById(batch.getProcessUserId());
        String recipients = joinEmails(sponsor, process);
        createAction(batch, "UNLOCK", PACKAGE_STATUS_APPROVED, PACKAGE_STATUS_REVIEWING,
                null, reason.trim(), actor, recipients,
                "申请包已由管理员解锁：" + safe(batch.getDisclosureName()),
                "<p>解锁原因：" + safe(reason) + "</p><p>申请包已回到审核中。</p>");
        return toView(batch, true);
    }

    //TODO

    /**
     * 提交国知局
     * @param packageToken
     * @param submissionNo
     * @param submittedAt
     * @param receipt
     * @param actor
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchView submitCnipa(String packageToken, String submissionNo, Date submittedAt,
                                 MultipartFile receipt, LoginUser actor) {
        ApplicationPackageBatch batch = requireVisibleBatch(packageToken, actor);
        requireProcessOwner(batch, actor);
        if (PACKAGE_STATUS_SUBMITTED.equals(batch.getStatus())) {
            return toView(batch, true);
        }
        requireStatus(batch, PACKAGE_STATUS_APPROVED);
        if (!hasText(submissionNo)) {
            throw new BusinessException("国知局提交流水号不能为空");
        }
        if (submittedAt == null) {
            throw new BusinessException("国知局提交时间不能为空");
        }
        byte[] receiptBytes = readAndValidate(receipt, PACKAGE_DOCUMENT_RECEIPT);
        String fileUrl = upload(receipt);
        registerFileRollback(fileUrl);

        ApplicationPackage receiptEntity = buildFile(batch, PACKAGE_DOCUMENT_RECEIPT,
                PACKAGE_FILE_ROLE_RECEIPT, receipt, receiptBytes, fileUrl, 1, actor);
        if (!fileService.save(receiptEntity)) {
            throw new BusinessException("国知局回执保存失败");
        }

        PatentDisclosure disclosure = requireDisclosure(batch);
        if (!Objects.equals(disclosure.getSyncedToPatent(), 1)
                || disclosure.getPatentApplicationId() == null) {
            PatentNewApplication application = new PatentNewApplication();
            application.setInternalNo(disclosure.getInternalNo());
            application.setPatentName(disclosure.getDisclosureName());
            application.setApplicant(disclosure.getApplicant());
            application.setInventor(disclosure.getInventor());
            application.setSponsor(disclosure.getSponsor());
            application.setAgent(disclosure.getAgent());
            application.setPatentType(disclosure.getPatentType());
            application.setApplicationDate(submittedAt);
            application.setCreateTime(new Date());
            if (!newApplicationService.save(application)) {
                throw new BusinessException("新申请记录生成失败");
            }
            disclosure.setSyncedToPatent(1);
            disclosure.setPatentApplicationId(application.getId());
        }
        disclosure.setPatentStatus(DISCLOSURE_STATUS_REPORTED);
        if (!disclosureService.updateById(disclosure)) {
            throw new BusinessException("交底申报状态更新失败");
        }

        batch.setStatus(PACKAGE_STATUS_SUBMITTED);
        batch.setSubmittedUserId(actor.getUserId());
        batch.setSubmittedUserName(actor.getLoginName());
        batch.setSubmittedAt(submittedAt);
        batch.setCnipaSubmissionNo(submissionNo.trim());
        updateBatch(batch);
        User sponsor = userService.getById(batch.getSponsorUserId());
        createAction(batch, "SUBMIT_CNIPA", PACKAGE_STATUS_APPROVED, PACKAGE_STATUS_SUBMITTED,
                PACKAGE_DOCUMENT_RECEIPT, "已登记国知局提交，流水号：" + submissionNo.trim(),
                actor, sponsor == null ? null : sponsor.getEmail(),
                "申请包已提交国知局：" + safe(batch.getDisclosureName()),
                "<p>提交流水号：" + safe(submissionNo) + "</p>");
        return toView(batch, true);
    }

    @Override
    public List<ProcessOperatorView> processOperators(LoginUser actor) {
        requireAuthenticated(actor);
        if (!isAdmin(actor) && !isOrganizer(actor)) {
            throw new BusinessException("无权查询流程专员");
        }
        Role role = roleService.getById(ROLE_ID_PROCESS_OPERATOR);
        if (role == null || !"0".equals(role.getStatus()) || !"0".equals(role.getDelFlag())) {
            return List.of();
        }
        List<Long> userIds = userRoleService.list(new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getRoleId, role.getRoleId())).stream()
                .map(UserRole::getUserId).distinct().toList();
        if (userIds.isEmpty()) {
            return List.of();
        }
        return userService.listByIds(userIds).stream()
                .filter(user -> "0".equals(user.getStatus()) && "0".equals(user.getDelFlag()))
                .map(user -> {
                    ProcessOperatorView view = new ProcessOperatorView();
                    view.setUserId(user.getUserId());
                    view.setUserName(displayName(user.getUserName(), user.getLoginName()));
                    view.setLoginName(user.getLoginName());
                    view.setEmail(user.getEmail());
                    return view;
                }).toList();
    }

    /**
     * 创建下载票据
     * @param fileToken
     * @param actor
     * @return
     */
    @Override
    public DownloadTicketView createDownloadTicket(String fileToken, LoginUser actor) {
        requireAuthenticated(actor);
        ApplicationPackage file = fileService.getOne(new LambdaQueryWrapper<ApplicationPackage>()
                .eq(ApplicationPackage::getFilePublicId, fileToken), false);
        if (file == null) {
            throw invisible();
        }
        ApplicationPackageBatch batch = batchService.getById(file.getBatchId());
        if (batch == null || !canView(batch, actor)) {
            throw invisible();
        }
        String ticket = UUID.randomUUID().toString();
        String value = actor.getUserId() + "|" + fileToken;
        if (!redisUtils.set(DOWNLOAD_KEY_PREFIX + ticket, value, DOWNLOAD_TICKET_SECONDS)) {
            throw new BusinessException("下载凭证生成失败，请稍后重试");
        }
        DownloadTicketView view = new DownloadTicketView();
        view.setTicket(ticket);
        view.setDownloadUrl("/api/application-package/download/" + ticket);
        view.setExpiresInSeconds(DOWNLOAD_TICKET_SECONDS);
        return view;
    }

    /**
     * 下载文件
     * @param ticket
     * @param actor
     * @return
     */
    @Override
    public ResponseEntity<Resource> download(String ticket, LoginUser actor) {
        requireAuthenticated(actor);
        String redisKey = DOWNLOAD_KEY_PREFIX + ticket;
        Object cached = redisUtils.getAndDelete(redisKey);
        if (!(cached instanceof String value)) {
            return ResponseEntity.notFound().build();
        }
        String[] parts = value.split("\\|", 2);
        if (parts.length != 2 || !Objects.equals(parts[0], String.valueOf(actor.getUserId()))) {
            return ResponseEntity.notFound().build();
        }
        ApplicationPackage file = fileService.getOne(new LambdaQueryWrapper<ApplicationPackage>()
                .eq(ApplicationPackage::getFilePublicId, parts[1]), false);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        ApplicationPackageBatch batch = batchService.getById(file.getBatchId());
        if (batch == null || !canView(batch, actor)) {
            return ResponseEntity.notFound().build();
        }
        String path = file.getFileUrl();
        if (!hasText(path)) {
            return ResponseEntity.notFound().build();
        }
        int query = path.indexOf('?');
        if (query >= 0) {
            path = path.substring(0, query);
        }
        String fileId = path.substring(path.lastIndexOf('/') + 1);
        return uploadFileService.getFile(fileId, file.getFileName());
    }

    private void applyDataScope(LambdaQueryWrapper<ApplicationPackageBatch> wrapper, LoginUser actor) {
        if (isAdmin(actor)) {
            return;
        }
        if (isProcess(actor)) {
            wrapper.eq(ApplicationPackageBatch::getProcessUserId, actor.getUserId())
                    .ne(ApplicationPackageBatch::getStatus, PACKAGE_STATUS_DRAFT);
            return;
        }
        if (isOrganizer(actor)) {
            wrapper.eq(ApplicationPackageBatch::getSponsorUserId, actor.getUserId());
            return;
        }
        wrapper.eq(ApplicationPackageBatch::getId, -1L);
    }

    private ApplicationPackageBatch requireVisibleBatch(String token, LoginUser actor) {
        requireAuthenticated(actor);
        ApplicationPackageBatch batch = batchService.getOne(
                new LambdaQueryWrapper<ApplicationPackageBatch>()
                        .eq(ApplicationPackageBatch::getPublicId, token), false);
        if (batch == null || !canView(batch, actor)) {
            throw invisible();
        }
        return batch;
    }

    private boolean canView(ApplicationPackageBatch batch, LoginUser actor) {
        if (isAdmin(actor)) {
            return true;
        }
        if (isProcess(actor)) {
            return !PACKAGE_STATUS_DRAFT.equals(batch.getStatus())
                    && Objects.equals(batch.getProcessUserId(), actor.getUserId());
        }
        return isOrganizer(actor) && Objects.equals(batch.getSponsorUserId(), actor.getUserId());
    }

    private void requireOrganizerOwner(PatentDisclosure disclosure, LoginUser actor) {
        if (isAdmin(actor)) {
            return;
        }
        if (!isOrganizer(actor) || !Objects.equals(disclosure.getSponsorUserId(), actor.getUserId())) {
            throw new BusinessException("只有该交底的主办人员可以组建申请包");
        }
    }

    private void requireSponsorOwner(ApplicationPackageBatch batch, LoginUser actor) {
        if (!isAdmin(actor) && (!isOrganizer(actor)
                || !Objects.equals(batch.getSponsorUserId(), actor.getUserId()))) {
            throw invisible();
        }
    }

    private void requireProcessOwner(ApplicationPackageBatch batch, LoginUser actor) {
        if (!isAdmin(actor) && (!isProcess(actor)
                || !Objects.equals(batch.getProcessUserId(), actor.getUserId()))) {
            throw invisible();
        }
    }

    private User requireActiveProcessOperator(Long userId) {
        if (userId == null) {
            throw new BusinessException("请选择流程专员");
        }
        User user = userService.getById(userId);
        if (user == null || !"0".equals(user.getStatus()) || !"0".equals(user.getDelFlag())) {
            throw new BusinessException("流程专员不存在或已停用");
        }
        Role role = roleService.getById(ROLE_ID_PROCESS_OPERATOR);
        boolean linked = role != null && "0".equals(role.getStatus()) && "0".equals(role.getDelFlag())
                && userRoleService.count(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, ROLE_ID_PROCESS_OPERATOR)) > 0;
        if (!linked) {
            throw new BusinessException("所选用户不是流程专员");
        }
        return user;
    }

    private void validateCompletePackage(ApplicationPackageBatch batch) {
        List<ApplicationPackage> files = fileService.list(new LambdaQueryWrapper<ApplicationPackage>()
                .eq(ApplicationPackage::getBatchId, batch.getId())
                .eq(ApplicationPackage::getFileRole, PACKAGE_FILE_ROLE_DOCUMENT)
                .in(ApplicationPackage::getDocumentCode, PACKAGE_DOCUMENT_CODES)
                .eq(ApplicationPackage::getIsCurrent, 1));
        Set<String> codes = files.stream().map(ApplicationPackage::getDocumentCode)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (files.size() != PACKAGE_DOCUMENT_CODES.size() || !codes.equals(PACKAGE_DOCUMENT_CODES)) {
            Set<String> missing = new LinkedHashSet<>(PACKAGE_DOCUMENT_CODES);
            missing.removeAll(codes);
            throw new BusinessException("申请包文件不完整，缺少："
                    + missing.stream().map(this::documentLabel).collect(Collectors.joining("、")));
        }
    }

    private ApplicationPackage buildFile(ApplicationPackageBatch batch, String code, String role,
                                           MultipartFile file, byte[] bytes, String fileUrl,
                                           int version, LoginUser actor) {
        String name = file.getOriginalFilename();
        String ext = extension(name);
        ApplicationPackage entity = new ApplicationPackage();
        entity.setBatchId(batch.getId());
        entity.setFilePublicId(UUID.randomUUID().toString());
        entity.setDisclosureId(batch.getDisclosureId());
        entity.setInternalNo(batch.getInternalNo());
        entity.setPackageType(PACKAGE_DOCUMENT_XML.equals(code) || PACKAGE_DOCUMENT_RECEIPT.equals(code)
                ? APPLICATION_PACKAGE_XML : APPLICATION_PACKAGE_FIVE_BOOKS);
        entity.setDocumentCode(code);
        entity.setFileRole(role);
        entity.setFileName(name);
        entity.setFileExt(ext);
        entity.setFilePath(fileUrl);
        entity.setFileUrl(fileUrl);
        entity.setFileSize(file.getSize());
        entity.setContentType(file.getContentType());
        entity.setSha256(sha256(bytes));
        entity.setVersionNo(version);
        entity.setIsCurrent(1);
        entity.setUploadUserId(actor.getUserId());
        entity.setUploadUserName(actor.getLoginName());
        entity.setUploadTime(new Date());
        entity.setConfirmStatus(APPLICATION_PACKAGE_UNCONFIRMED);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        return entity;
    }

    private byte[] readAndValidate(MultipartFile file, String documentCode) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("单个文件不能超过10MB");
        }
        try {
            byte[] bytes = file.getBytes();
            String ext = extension(file.getOriginalFilename());
            if (PACKAGE_DOCUMENT_XML.equals(documentCode) || PACKAGE_DOCUMENT_RECEIPT.equals(documentCode)) {
                if (!"xml".equals(ext)) {
                    throw new BusinessException("XML申请文件和国知局回执只能上传.xml文件");
                }
                validateXml(bytes);
            } else {
                if (!WORD_EXTENSIONS.contains(ext)) {
                    throw new BusinessException(documentLabel(documentCode) + "只能上传.doc或.docx文件");
                }
                if ("docx".equals(ext)) {
                    validateDocx(bytes);
                } else {
                    validateDoc(bytes);
                }
            }
            return bytes;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("文件读取或格式校验失败");
        }
    }

    private void validateXml(byte[] bytes) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
    }

    private void validateDocx(byte[] bytes) throws Exception {
        boolean contentTypes = false;
        boolean document = false;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if ("[Content_Types].xml".equals(entry.getName())) contentTypes = true;
                if ("word/document.xml".equals(entry.getName())) document = true;
            }
        }
        if (!contentTypes || !document) {
            throw new BusinessException("文件不是有效的.docx文档");
        }
    }

    private void validateDoc(byte[] bytes) {
        byte[] ole = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1};
        if (bytes.length < ole.length) {
            throw new BusinessException("文件不是有效的.doc文档");
        }
        for (int i = 0; i < ole.length; i++) {
            if (bytes[i] != ole[i]) {
                throw new BusinessException("文件不是有效的.doc文档");
            }
        }
    }

    private String upload(MultipartFile file) {
        Result<String> result = uploadFileService.upload(file);
        if (result.getCode() != 200 || !hasText(result.getData())) {
            throw new BusinessException(result.getMessage());
        }
        return result.getData();
    }

    private void registerFileRollback(String fileUrl) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != TransactionSynchronization.STATUS_COMMITTED) {
                        uploadFileService.deleteByUrl(fileUrl);
                    }
                }
            });
        }
    }

    private void updateBatch(ApplicationPackageBatch batch) {
        if (!batchService.updateById(batch)) {
            throw new BusinessException("申请包已被其他操作更新，请刷新后重试");
        }
    }

    /**
     * 创造操作记录
     * @param batch
     * @param action
     * @param from
     * @param to
     * @param documentCode
     * @param remark
     * @param actor
     * @param recipientEmails
     * @param subject
     * @param content
     * @return
     */
    private ApplicationPackageActionLog createAction(
            ApplicationPackageBatch batch, String action, String from, String to,
            String documentCode, String remark, LoginUser actor,
            String recipientEmails, String subject, String content) {
        ApplicationPackageActionLog log = new ApplicationPackageActionLog();
        log.setBatchId(batch.getId());
        log.setRoundNo(batch.getRoundNo());
        log.setActionType(action);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setDocumentCode(documentCode);
        log.setOperatorUserId(actor.getUserId());
        log.setOperatorUserName(actor.getLoginName());
        log.setRemark(remark);
        log.setCreateTime(new Date());
        if (subject != null) {
            log.setMailStatus("PENDING");
        }
        actionLogService.save(log);
        if (subject != null) {
            eventPublisher.publishEvent(new ApplicationPackageMailEvent(
                    log.getId(), recipientEmails, subject, content, batch.getPublicId(), action));
        }
        return log;
    }

    private BatchView toView(ApplicationPackageBatch batch, boolean includeDetails) {
        BatchView view = new BatchView();
        view.setPackageToken(batch.getPublicId());
        view.setDisclosureId(batch.getDisclosureId());
        view.setInternalNo(batch.getInternalNo());
        view.setDisclosureName(batch.getDisclosureName());
        view.setSponsorUserId(batch.getSponsorUserId());
        view.setSponsorUserName(batch.getSponsorUserName());
        view.setProcessUserId(batch.getProcessUserId());
        view.setProcessUserName(batch.getProcessUserName());
        view.setStatus(batch.getStatus());
        view.setRoundNo(batch.getRoundNo());
        view.setSentAt(batch.getSentAt());
        view.setReceivedAt(batch.getReceivedAt());
        view.setRejectedAt(batch.getRejectedAt());
        view.setRejectReason(batch.getRejectReason());
        view.setApprovedUserName(batch.getApprovedUserName());
        view.setApprovedAt(batch.getApprovedAt());
        view.setUnlockedUserName(batch.getUnlockedUserName());
        view.setUnlockedAt(batch.getUnlockedAt());
        view.setSubmittedUserName(batch.getSubmittedUserName());
        view.setSubmittedAt(batch.getSubmittedAt());
        view.setCnipaSubmissionNo(batch.getCnipaSubmissionNo());
        view.setCreateTime(batch.getCreateTime());
        view.setUpdateTime(batch.getUpdateTime());
        if (!includeDetails) {
            return view;
        }
        List<ApplicationPackage> files = fileService.list(new LambdaQueryWrapper<ApplicationPackage>()
                .eq(ApplicationPackage::getBatchId, batch.getId())
                .orderByDesc(ApplicationPackage::getCreateTime)
                .orderByDesc(ApplicationPackage::getId));
        for (ApplicationPackage file : files) {
            FileView fileView = toFileView(file);
            if (Objects.equals(file.getIsCurrent(), 1)) view.getCurrentFiles().add(fileView);
            else view.getFileHistory().add(fileView);
        }
        view.setIssues(issueService.list(new LambdaQueryWrapper<ApplicationPackageReviewIssue>()
                        .eq(ApplicationPackageReviewIssue::getBatchId, batch.getId())
                        .orderByDesc(ApplicationPackageReviewIssue::getCreateTime)
                        .orderByDesc(ApplicationPackageReviewIssue::getId)).stream()
                .map(this::toIssueView).toList());
        view.setActions(actionLogService.list(new LambdaQueryWrapper<ApplicationPackageActionLog>()
                        .eq(ApplicationPackageActionLog::getBatchId, batch.getId())
                        .orderByDesc(ApplicationPackageActionLog::getCreateTime)
                        .orderByDesc(ApplicationPackageActionLog::getId)).stream()
                .map(this::toActionView).toList());
        return view;
    }

    private FileView toFileView(ApplicationPackage file) {
        FileView view = new FileView();
        view.setFileToken(file.getFilePublicId());
        view.setDocumentCode(file.getDocumentCode());
        view.setFileRole(file.getFileRole());
        view.setFileName(file.getFileName());
        view.setFileExt(file.getFileExt());
        view.setFileSize(file.getFileSize());
        view.setContentType(file.getContentType());
        view.setSha256(file.getSha256());
        view.setVersionNo(file.getVersionNo());
        view.setIsCurrent(file.getIsCurrent());
        view.setUploadUserName(file.getUploadUserName());
        view.setUploadTime(file.getUploadTime());
        return view;
    }

    private IssueView toIssueView(ApplicationPackageReviewIssue issue) {
        IssueView view = new IssueView();
        view.setRoundNo(issue.getRoundNo());
        view.setDocumentCode(issue.getDocumentCode());
        view.setIssueText(issue.getIssueText());
        view.setReviewerUserName(issue.getReviewerUserName());
        view.setCreateTime(issue.getCreateTime());
        return view;
    }

    private ActionView toActionView(ApplicationPackageActionLog action) {
        ActionView view = new ActionView();
        view.setRoundNo(action.getRoundNo());
        view.setActionType(action.getActionType());
        view.setFromStatus(action.getFromStatus());
        view.setToStatus(action.getToStatus());
        view.setDocumentCode(action.getDocumentCode());
        view.setOperatorUserName(action.getOperatorUserName());
        view.setRemark(action.getRemark());
        view.setMailStatus(action.getMailStatus());
        view.setMailError(action.getMailError());
        view.setCreateTime(action.getCreateTime());
        return view;
    }

    private PatentDisclosure requireDisclosure(ApplicationPackageBatch batch) {
        PatentDisclosure disclosure = disclosureService.getById(batch.getDisclosureId());
        if (disclosure == null) throw new BusinessException("交底记录不存在");
        return disclosure;
    }

    private void requireStatus(ApplicationPackageBatch batch, String expected) {
        if (!expected.equals(batch.getStatus())) {
            throw new BusinessException("当前申请包状态不允许此操作");
        }
    }

    private void requireAuthenticated(LoginUser actor) {
        if (actor == null || actor.getUserId() == null) {
            throw new BusinessException(401, "登录状态已失效");
        }
    }

    private boolean isAdmin(LoginUser actor) { return hasRole(actor, ROLE_ADMIN); }
    private boolean isOrganizer(LoginUser actor) { return hasRole(actor, ROLE_ORGANIZER); }
    private boolean isProcess(LoginUser actor) { return hasRole(actor, ROLE_PROCESS_OPERATOR); }
    private boolean hasRole(LoginUser actor, String role) {
        return actor != null && actor.getRoles() != null && actor.getRoles().contains(role);
    }

    private BusinessException invisible() {
        return new BusinessException("申请包不存在或无权访问");
    }

    private String extension(String name) {
        if (!hasText(name) || !name.contains(".")) return "";
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String sha256(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte value : digest) builder.append(String.format("%02x", value));
            return builder.toString();
        } catch (Exception e) {
            throw new BusinessException("文件摘要计算失败");
        }
    }

    private String documentLabel(String code) {
        if (code == null) return "整包";
        return switch (code) {
            case PACKAGE_DOCUMENT_XML -> "XML申请文件";
            case PACKAGE_DOCUMENT_REQUEST -> "请求书";
            case PACKAGE_DOCUMENT_DESCRIPTION -> "说明书";
            case PACKAGE_DOCUMENT_CLAIMS -> "权利要求书";
            case PACKAGE_DOCUMENT_ABSTRACT -> "摘要";
            case PACKAGE_DOCUMENT_ABSTRACT_DRAWING -> "摘要附图";
            case PACKAGE_DOCUMENT_RECEIPT -> "国知局回执";
            default -> code;
        };
    }

    private String joinEmails(User... users) {
        return Arrays.stream(users).filter(Objects::nonNull).map(User::getEmail)
                .filter(ApplicationPackageWorkflowServiceImpl::hasText)
                .distinct().collect(Collectors.joining(","));
    }

    private static boolean hasText(String value) { return value != null && !value.isBlank(); }
    private static int defaultInt(Integer value, int defaultValue) { return value == null ? defaultValue : value; }
    private static String displayName(String preferred, String fallback) {
        return hasText(preferred) ? preferred : fallback;
    }
    private static String safe(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
