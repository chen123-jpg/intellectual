package com.intellectual.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 中间著变专利表（有重复）
 *
 * @author 陈创
 * @since 2026-07-25 18:12
 */
@Data
@TableName("patent_intermediate_change")
public class PatentIntermediateChange implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 序号
     */
    private Integer seqNo;

    /**
     * 内部编号
     */
    private String internalNo;

    /**
     * 业务类型（转让/转我所/著录变更等）
     */
    private String businessType;

    /**
     * 申请号
     */
    private String applicationNo;

    /**
     * 发明创造名称
     */
    private String patentName;

    /**
     * 申请人
     */
    private String applicant;

    /**
     * 发明人
     */
    private String inventor;

    /**
     * 主办人
     */
    private String sponsor;

    /**
     * 委托书代理人
     */
    private String agent;

    /**
     * 申请日
     */
    private Date applicationDate;

    /**
     * 状态
     */
    private String status;

    /**
     * 发文日
     */
    private Date issueDate;

    /**
     * 非正标-费减情况
     */
    private String feeReductionInfo;

    /**
     * 提交日期
     */
    private Date submitDate;

    /**
     * 缴费止期
     */
    private Date paymentDeadline;

    /**
     * 费用金额
     */
    private BigDecimal feeAmount;

    /**
     * 缴费状态
     */
    private String paymentStatus;

    /**
     * 备注1
     */
    private String remark1;

    /**
     * 备注2
     */
    private String remark2;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
