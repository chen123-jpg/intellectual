package com.intellectual.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 提醒规则表
 *
 * @author 陈创
 * @since 2026-08-01 17:00
 */
@Data
@TableName("reminder_rule")
public class ReminderRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 案件ID，NULL代表全局通用规则
     */
    private Long caseId;

    /**
     * 期限类型：PAY_FEE/SUPPLEMENT/REPLY_OFFICE_ACTION
     */
    private String deadlineType;

    /**
     * 规则类型：OFFSET-时间偏移 / PERCENT-剩余百分比
     */
    private String ruleType;

    /**
     * 偏移量数值，OFFSET类型生效
     */
    private Integer offsetValue;

    /**
     * 偏移单位：DAY/HOUR/MINUTE，OFFSET类型生效
     */
    private String offsetUnit;

    /**
     * 剩余百分比数值(1-99)，PERCENT类型生效
     */
    private Integer percentValue;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer isActive;

    /**
     * 规则创建时间
     */
    private Date createTime;
}
