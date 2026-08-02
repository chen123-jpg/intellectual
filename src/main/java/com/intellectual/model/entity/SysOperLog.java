package com.intellectual.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 操作日志表
 *
 * @author 陈创
 * @since 2026-08-01 17:00
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 操作类型：CREATE_RULE/UPDATE_RULE/DELETE_RULE/BATCH_PERCENT/MARK_READ
     */
    private String operType;

    /**
     * 操作内容（JSON格式：变更前后数据）
     */
    private String content;

    /**
     * 操作时间
     */
    private Date operTime;

    /**
     * 客户端IP
     */
    private String ip;
}
