package com.intellectual.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 专利新申请表
 *
 * @author 陈创
 * @since 2026-07-25 18:12
 */
@Data
@TableName("patent_new_application")
public class PatentNewApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 内部编号
     */
    private String internalNo;

    /**
     * 发明创造名称
     */
    private String patentName;

    /**
     * 申请号/专利号
     */
    private String applicationNo;

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
     * 通知书（状态子列）
     */
    private String notification;

    /**
     * 发文日（状态子列）
     */
    private Date issueDate;

    /**
     * 非正标-预审标（状态子列）
     */
    private String preExamMark;

    /**
     * 缴费止期（费用子列）
     */
    private Date paymentDeadline;

    /**
     * 费用金额（费用子列）
     */
    private BigDecimal feeAmount;

    /**
     * 缴费时间（费用子列）
     */
    private String paymentDate;

    /**
     * 序号
     */
    private Integer seqNo;

    /**
     * 类型（发明/实用新型/外观设计）
     */
    private String patentType;

    /**
     * DAS码
     */
    private String dasCode;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
