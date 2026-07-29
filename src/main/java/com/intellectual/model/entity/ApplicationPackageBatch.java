package com.intellectual.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("application_package_batch")
public class ApplicationPackageBatch implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String publicId;
    private Long disclosureId;
    private String internalNo;
    private String disclosureName;
    private Long sponsorUserId;
    private String sponsorUserName;
    private Long processUserId;
    private String processUserName;
    private String status;
    private Integer roundNo;
    @Version
    private Integer lockVersion;
    private Date sentAt;
    private Date receivedAt;
    private Date rejectedAt;
    private String rejectReason;
    private Long approvedUserId;
    private String approvedUserName;
    private Date approvedAt;
    private Long unlockedUserId;
    private String unlockedUserName;
    private Date unlockedAt;
    private Long submittedUserId;
    private String submittedUserName;
    private Date submittedAt;
    private String cnipaSubmissionNo;
    private Date createTime;
    private Date updateTime;
}
