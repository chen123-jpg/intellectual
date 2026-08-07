package com.intellectual.model.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.SqlCondition;

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
    
    @TableField(condition = SqlCondition.LIKE)
    private String internalNo;

    /**
     * 业务类型（转让/转我所/著录变更等）
     */
    private String businessType;

    /**
     * 申请号
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String applicationNo;

    /**
     * 发明创造名称
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String patentName;

    /**
     * 申请人
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String applicant;

    /**
     * 发明人
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String inventor;

    /**
     * 主办人
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String sponsor;

    /**
     * 委托书代理人
     */
    
    @TableField(condition = SqlCondition.LIKE)
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
    
    @TableField(condition = SqlCondition.LIKE)
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
    
    @TableField(condition = SqlCondition.LIKE)
    private String paymentStatus;

    /**
     * 备注1
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String remark1;

    /**
     * 备注2
     */
    
    @TableField(condition = SqlCondition.LIKE)
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
