package com.intellectual.model.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.SqlCondition;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * PCT国际申请表
 *
 * @author 陈创
 * @since 2026-07-25 18:12
 */
@Data
@TableName("patent_pct")
public class PatentPct implements Serializable {

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
     * PCT内部编号
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String pctInternalNo;

    /**
     * 状态
     */
    private String status;

    /**
     * 发文日
     */
    private Date issueDate;

    /**
     * 在先内部编号
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String priorInternalNo;

    /**
     * 在先申请号
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String priorApplicationNo;

    /**
     * 在先申请日
     */
    private Date priorApplicationDate;

    /**
     * PCT申请日
     */
    private Date pctApplicationDate;

    /**
     * 申请名称
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String applicationName;

    /**
     * 申请号（PCT号）
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String applicationNo;

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
     * 初检结论
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String preliminaryConclusion;

    /**
     * 备注
     */
    
    @TableField(condition = SqlCondition.LIKE)
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
