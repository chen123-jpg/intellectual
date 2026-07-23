package com.intellectual.model.entity;

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
 * @since 2026-07-23 16:59
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
    private String priorInternalNo;

    /**
     * 在先申请号
     */
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
    private String applicationName;

    /**
     * 申请号（PCT号）
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
     * 初检结论
     */
    private String preliminaryConclusion;

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
