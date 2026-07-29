package com.intellectual.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 邮件发送记录表
 *
 * @author 陈创
 * @since 2026-07-23 19:09
 */
@Data
@TableName("mail_send_log")
public class MailSendLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 专利交底 id
     */
    private Long disclosureId;

    /**
     * 发件人邮箱
     */
    private String fromEmail;

    /**
     * 收件人，逗号分隔
     */
    private String toEmails;

    /**
     * 抄送，逗号分隔
     */
    private String ccEmails;

    /**
     * 实际发送主题
     */
    private String subject;

    /**
     * 实际发送正文
     */
    private String content;

    /**
     * PENDING 0/SUCCESS 1/FAILED 2
     */
    private Integer sendStatus;

    /**
     * 失败原因
     */
    private String errorMessage;

    /**
     * 发送人用户ID
     */
    private Long senderUserId;

    /**
     * 发送人姓名
     */
    private String senderName;

    /**
     * 实际发送时间
     */
    private Date sentAt;

    /**
     * 创建时间
     */
    private Date createTime;
}
