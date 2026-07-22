package com.intellectual.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 邮件发送附件表
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
@Data
@TableName("mail_send_attachment")
public class MailSendAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发信记录ID
     */
    private Long mailSendLogId;

    /**
     * 来源交底附件ID，可空(用户临时上传)
     */
    private Long disclosureAttachmentId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 路径
     */
    private String filePath;

    /**
     * 访问URL
     */
    private String fileUrl;

    /**
     * 字节数
     */
    private Long fileSize;

    /**
     * 创建时间
     */
    private Date createTime;
}
