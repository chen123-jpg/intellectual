package com.intellectual.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 交底状态变更日志
 *
 * @author 陈创
 * @since 2026-07-23 16:59
 */
@Data
@TableName("disclosure_status_log")
public class DisclosureStatusLog implements Serializable {

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
     * 原状态(码或文案)
     */
    private String fromStatus;

    /**
     * 新状态
     */
    private String toStatus;

    /**
     * 操作人ID
     */
    private Long operatorUserId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 备注/原因
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;
}
