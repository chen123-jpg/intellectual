package com.intellectual.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class ApplicationPackageWorkflowDtos {
    private ApplicationPackageWorkflowDtos() {
    }

    @Data
    public static class DraftRequest {
        private Long disclosureId;
    }

    @Data
    public static class SendRequest {
        private Long processUserId;
    }

    @Data
    public static class RejectRequest {
        private String reason;
        private List<IssueRequest> issues = new ArrayList<>();
    }

    @Data
    public static class IssueRequest {
        private String documentCode;
        private String issueText;
    }

    @Data
    public static class UnlockRequest {
        private String reason;
    }

    @Data
    public static class BatchView {
        private String packageToken;
        private Long disclosureId;
        private String internalNo;
        private String disclosureName;
        private Long sponsorUserId;
        private String sponsorUserName;
        private Long processUserId;
        private String processUserName;
        private String status;
        private Integer roundNo;
        private Date sentAt;
        private Date receivedAt;
        private Date rejectedAt;
        private String rejectReason;
        private String approvedUserName;
        private Date approvedAt;
        private String unlockedUserName;
        private Date unlockedAt;
        private String submittedUserName;
        private Date submittedAt;
        private String cnipaSubmissionNo;
        private Date createTime;
        private Date updateTime;
        private List<FileView> currentFiles = new ArrayList<>();
        private List<FileView> fileHistory = new ArrayList<>();
        private List<IssueView> issues = new ArrayList<>();
        private List<ActionView> actions = new ArrayList<>();
    }

    @Data
    public static class FileView {
        private String fileToken;
        private String documentCode;
        private String fileRole;
        private String fileName;
        private String fileExt;
        private Long fileSize;
        private String contentType;
        private String sha256;
        private Integer versionNo;
        private Integer isCurrent;
        private String uploadUserName;
        private Date uploadTime;
    }

    @Data
    public static class IssueView {
        private Integer roundNo;
        private String documentCode;
        private String issueText;
        private String reviewerUserName;
        private Date createTime;
    }

    @Data
    public static class ActionView {
        private Integer roundNo;
        private String actionType;
        private String fromStatus;
        private String toStatus;
        private String documentCode;
        private String operatorUserName;
        private String remark;
        private String mailStatus;
        private String mailError;
        private Date createTime;
    }

    @Data
    public static class ProcessOperatorView {
        private Long userId;
        private String userName;
        private String loginName;
        private String email;
    }

    @Data
    public static class DownloadTicketView {
        private String ticket;
        private String downloadUrl;
        private long expiresInSeconds;
    }
}
