package com.intellectual.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 补漏专利表
 *
 * @author 陈创
 * @since 2026-07-23 16:59
 */
@Data
@TableName("patent_supplementary")
public class PatentSupplementary implements Serializable {

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
     * 申请号/专利号
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
     * 通知书（状态下子列）
     */
    private String notification;

    /**
     * 发文日（状态下子列）
     */
    private Date issueDate;

    /**
     * 费减（状态下子列）
     */
    private String feeReduction;

    /**
     * 备注
     */
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
