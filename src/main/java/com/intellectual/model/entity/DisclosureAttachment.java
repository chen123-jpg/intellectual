package com.intellectual.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 交底附件表
 *
 * @author 陈创
 * @since 2026-07-23 16:59
 */
@Data
@TableName("disclosure_attachment")
public class DisclosureAttachment implements Serializable {

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
     * 内部编号(冗余)
     */
    private String internalNo;

    /**
     * DISCLOSURE_DOC / DISCLOSURE_OTHER / MAIL_EXTRA
     */
    private String bizType;

    /**
     * 关联发信记录(仅MAIL_EXTRA时用)
     */
    private Long mailSendLogId;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 扩展名，不含点
     */
    private String fileExt;

    /**
     * 存储相对/绝对路径
     */
    private String filePath;

    /**
     * 访问URL，如 /files/xxx.docx?name=...
     */
    private String fileUrl;

    /**
     * 字节数
     */
    private Long fileSize;

    /**
     * MIME
     */
    private String contentType;

    /**
     * 是否必填类: 交底书=1
     */
    private Integer isRequired;

    /**
     * 排序，小在前
     */
    private Integer sortNo;

    /**
     * 上传人ID
     */
    private Long uploadUserId;

    /**
     * 上传人姓名
     */
    private String uploadUserName;

    /**
     * 逻辑删除: 0否 1是
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
