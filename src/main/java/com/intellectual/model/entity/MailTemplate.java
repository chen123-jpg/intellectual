package com.intellectual.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 邮件模板表
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
@Data
@TableName("mail_template")
public class MailTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板编码
     */
    private String templateCode;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 主题模板，支持占位符
     */
    private String subject;

    /**
     * 正文模板，支持占位符
     */
    private String content;

    /**
     * 默认附带附件类型，逗号分隔如 DISCLOSURE_DOC
     */
    private String defaultAttachTypes;

    /**
     * 是否启用: 0否 1是
     */
    private Integer enabled;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
