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
 * 复审无效专利表
 *
 * @author 陈创
 * @since 2026-07-25 18:12
 */
@Data
@TableName("patent_reexamination")
public class PatentReexamination implements Serializable {

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
     * 类型（发明/实用新型/外观设计）
     */
    private String patentType;

    /**
     * 分类（复审/无效）
     */
    private String category;

    /**
     * 案件编号
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String caseNo;

    /**
     * 内部编号
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String internalNo;

    /**
     * 申请号
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String applicationNo;

    /**
     * 专利名称
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String patentName;

    /**
     * 申请人
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String applicant;

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
     * 通知书
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String notification;

    /**
     * 发文日
     */
    private Date issueDate;

    /**
     * 提交日期
     */
    private Date submitDate;

    /**
     * 25.6.12查询
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String queryInfo;

    /**
     * 规费
     */
    private BigDecimal officialFee;

    /**
     * 缴费时间
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String paymentDate;

    /**
     * 附注1
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String note1;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
