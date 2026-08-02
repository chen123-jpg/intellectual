package com.intellectual.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 案件期限表
 *
 * @author 陈创
 * @since 2026-08-01 17:00
 */
@Data
@TableName("case_deadline")
public class CaseDeadline implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联案件ID
     */
    private Long caseId;

    /**
     * 期限类型：PAY_FEE/SUPPLEMENT/REPLY_OFFICE_ACTION
     */
    private String deadlineType;

    /**
     * 截止绝对时间
     */
    private Date deadlineTime;

    /**
     * 期限起始时间（发文日），与deadline_time组合计算总时长
     */
    private Date startTime;

    /**
     * 状态：PENDING-待处理 / COMPLETED-已完成 / EXPIRED-已过期
     */
    private String status;

    /**
     * 是否估算计算：0=精确计算(有start_time)，1=使用默认天数估算
     */
    private Integer isEstimateCalc;

    /**
     * 记录创建时间
     */
    private Date createTime;
}
