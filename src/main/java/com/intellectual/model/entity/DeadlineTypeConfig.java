package com.intellectual.model.entity;

import java.io.Serializable;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 期限类型配置表
 *
 * @author 陈创
 * @since 2026-08-01 17:00
 */
@Data
@TableName("deadline_type_config")
public class DeadlineTypeConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 期限类型
     */
    private String deadlineType;

    /**
     * 默认总时长（天），用于百分比兜底计算
     */
    private Integer defaultDays;

    /**
     * 类型描述
     */
    private String description;
}
