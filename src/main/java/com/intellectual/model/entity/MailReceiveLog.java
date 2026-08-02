package com.intellectual.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 邮件接收记录表
 *
 * @author 陈创
 * @since 2026-08-02
 */
@Data
@TableName("mail_receive_log")
public class MailReceiveLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发件人邮箱
     */
    private String fromEmail;

    /**
     * 收件人，逗号分隔
     */
    private String toEmails;

    /**
     * 主题
     */
    private String subject;

    /**
     * 正文
     */
    private String content;

    /**
     * 收件人用户ID
     */
    private Long senderUserId;

    /**
     * 收件人姓名
     */
    private String senderName;

    /**
     * 接收时间
     */
    private Date receivedAt;

    /**
     * 创建时间
     */
    private Date createTime;
}
