package com.intellectual.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 申请包表(XML包与五书WORD分条目)
 *
 * @author 陈创
 * @since 2026-07-23 16:59
 */
@Data
@TableName("application_package")
public class ApplicationPackage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 申请包批次内部ID */
    private Long batchId;

    /** 对外文件令牌 */
    private String filePublicId;

    /**
     * 交底ID
     */
    private Long disclosureId;

    /**
     * 内部编号
     */
    private String internalNo;

    /**
     * XML_PACKAGE / FIVE_BOOKS_WORD
     */
    private String packageType;

    /** XML/REQUEST/DESCRIPTION/CLAIMS/ABSTRACT/ABSTRACT_DRAWING */
    private String documentCode;

    /** PACKAGE_DOCUMENT / CNIPA_RECEIPT */
    private String fileRole;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 扩展名
     */
    private String fileExt;

    /**
     * 存储路径
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
     * MIME
     */
    private String contentType;

    /** 文件内容SHA-256 */
    private String sha256;

    /**
     * 版本号，覆盖上传+1
     */
    private Integer versionNo;

    /**
     * 是否当前有效版本: 0否 1是
     */
    private Integer isCurrent;

    /**
     * 仅当前版本参与唯一（MySQL生成列，不允许手动插入/更新）
     */
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String currentTypeKey;

    /** 当前槽位唯一键（MySQL生成列） */
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String currentSlotKey;

    /**
     * 上传人ID(主办)
     */
    private Long uploadUserId;

    /**
     * 上传人姓名
     */
    private String uploadUserName;

    /**
     * 上传时间
     */
    private Date uploadTime;

    /**
     * UNCONFIRMED未确认 / CONFIRMED可提交 / SUBMITTED已交国知局
     */
    private String confirmStatus;

    /**
     * 确认人ID(流程)
     */
    private Long confirmUserId;

    /**
     * 确认人姓名
     */
    private String confirmUserName;

    /**
     * 确认时间
     */
    private Date confirmTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
