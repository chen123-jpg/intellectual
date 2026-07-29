package com.intellectual.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("application_package_action_log")
public class ApplicationPackageActionLog implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long batchId;
    private Integer roundNo;
    private String actionType;
    private String fromStatus;
    private String toStatus;
    private String documentCode;
    private Long operatorUserId;
    private String operatorUserName;
    private String remark;
    private String mailStatus;
    private Long mailLogId;
    private String mailError;
    private Date createTime;
}
