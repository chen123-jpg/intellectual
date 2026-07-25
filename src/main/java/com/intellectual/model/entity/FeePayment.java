package com.intellectual.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 缴费表
 *
 * @author 陈创
 * @since 2026-07-26 00:42
 */
@Data
@TableName("fee_payment")
public class FeePayment implements Serializable {

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
     * 申请人/缴费主体
     */
    private String applicant;

    /**
     * 费用类型: 官费/代理费等
     */
    private String feeType;

    /**
     * 金额
     */
    private BigDecimal feeAmount;

    /**
     * 缴费止期
     */
    private Date paymentDeadline;

    /**
     * 实缴日期
     */
    private Date paymentDate;

    /**
     * PENDING待缴/PAID已缴/PARTIAL部分/VOID作废
     */
    private String paymentStatus;

    /**
     * 付款方
     */
    private String payer;

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
