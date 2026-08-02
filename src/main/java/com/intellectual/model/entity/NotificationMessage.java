package com.intellectual.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 提醒消息表
 *
 * @author 陈创
 * @since 2026-08-01 17:00
 */
@Data
@TableName("notification_message")
public class NotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消息接收用户ID
     */
    private Long userId;

    /**
     * 关联案件ID
     */
    private Long caseId;

    /**
     * 关联案件期限ID
     */
    private Long deadlineId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息正文详情
     */
    private String content;

    /**
     * 消息跳转路径（前端路由）
     */
    private String link;

    /**
     * 已读状态：0-未读，1-已读
     */
    private Integer isRead;

    /**
     * 实时推送状态：0-未推送，1-已推送
     */
    private Integer isPushed;

    /**
     * 计划推送时间（规则计算得出）
     */
    private Date plannedSendTime;

    /**
     * 实际推送时间
     */
    private Date actualSendTime;

    /**
     * 消息创建时间
     */
    private Date createTime;

    /**
     * 用户阅读时间
     */
    private Date readTime;

    /**
     * 是否估算时间：0=精确计算，1=使用默认天数估算（前端展示"估算值"提示）
     */
    private Integer isEstimateCalc;
}
