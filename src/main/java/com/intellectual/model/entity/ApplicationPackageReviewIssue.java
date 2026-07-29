package com.intellectual.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("application_package_review_issue")
public class ApplicationPackageReviewIssue implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long batchId;
    private Integer roundNo;
    private String documentCode;
    private String issueText;
    private Long reviewerUserId;
    private String reviewerUserName;
    private Date createTime;
}
