package com.intellectual.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 开票表
 *
 * @author 陈创
 * @since 2026-07-23 16:59
 */
@Data
@TableName("invoice")
public class Invoice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 交底ID
     */
    private Long disclosureId;

    /**
     * 内部编号
     */
    private String internalNo;

    /**
     * 临时编号
     */
    private String tempNo;

    /**
     * 交底/专利名称(冗余)
     */
    private String disclosureName;

    /**
     * 申请人
     */
    private String applicant;

    /**
     * 发票抬头
     */
    private String invoiceTitle;

    /**
     * 税号
     */
    private String taxNo;

    /**
     * 发票类型: 普票/专票等
     */
    private String invoiceType;

    /**
     * 开票金额
     */
    private BigDecimal invoiceAmount;

    /**
     * PENDING待开/ISSUED已开/VOID作废
     */
    private String invoiceStatus;

    /**
     * 发票号码
     */
    private String invoiceNo;

    /**
     * 开票日期
     */
    private Date invoiceDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 来源: DISCLOSURE_SYNC等
     */
    private String source;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
