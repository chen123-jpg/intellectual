package com.intellectual.model.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.SqlCondition;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 专利交底信息表（T表）
 *
 * @author 陈创
 * @since 2026-07-23 16:59
 */
@Data
@TableName("patent_disclosure")
public class PatentDisclosure implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 临时编号（根据交底日期生成，如P20260802）
     */

    @TableField(condition = SqlCondition.LIKE)
    private String tempNo;

    /**
     * 内部编号（与P表关联键）
     */

    @TableField(condition = SqlCondition.LIKE)
    private String internalNo;

    /**
     * 专利状态（如受理、N稿撰写中等）
     */

    @TableField(condition = SqlCondition.LIKE)
    private String patentStatus;

    /**
     * 要求（如一周内提交、追求授权等）
     */

    @TableField(condition = SqlCondition.LIKE)
    private String requirement;

    /**
     * 专利交底名称
     */

    @TableField(condition = SqlCondition.LIKE)
    private String disclosureName;

    /**
     * 申请人（可多个，用顿号或逗号分隔）
     */

    @TableField(condition = SqlCondition.LIKE)
    private String applicant;

    /**
     * 发明人
     */

    @TableField(condition = SqlCondition.LIKE)
    private String inventor;

    /**
     * 专利类型：发明/实用新型/外观等
     */
    private String patentType;

    /**
     * 是否邀请进群（0-否，1-是）
     */
    private Integer invitedToGroup;

    /**
     * 联系人姓名
     */

    @TableField(condition = SqlCondition.LIKE)
    private String contactPerson;

    /**
     * 管理人姓名
     */

    @TableField(condition = SqlCondition.LIKE)
    private String manager;

    /**
     * 代理人姓名
     */

    @TableField(condition = SqlCondition.LIKE)
    private String agent;

    /**
     * 指导人
     */
    @TableField(condition = SqlCondition.LIKE)
    private String mentor;

    /**
     * 业务人员
     */
    @TableField(condition = SqlCondition.LIKE)
    private String businessPersonnel;

    /**
     * 主办人姓名
     */

    @TableField(condition = SqlCondition.LIKE)
    private String sponsor;

    /**
     * 主办人用户ID
     */
    private Long sponsorUserId;

    /**
     * 交底日
     */
    private Date disclosureDate;

    /**
     * 交底天数（可为空）
     */
    private Integer disclosureDays;

    /**
     * 备注
     */

    @TableField(condition = SqlCondition.LIKE)
    private String remark;

    /**
     * 联系人信息（含电话、邮箱、QQ等）
     */

    @TableField(condition = SqlCondition.LIKE)
    private String contactInfo;

    /**
     * 联系人邮箱(发邮件主收件人)
     */

    @TableField(condition = SqlCondition.LIKE)
    private String contactEmail;

    /**
     * 联系人电话
     */

    @TableField(condition = SqlCondition.LIKE)
    private String contactPhone;

    /**
     * 录入人用户ID
     */
    private Long entryUserId;

    /**
     * 录入人姓名
     */

    @TableField(condition = SqlCondition.LIKE)
    private String entryUserName;

    /**
     * 复制来源交底ID
     */
    private Long copyFromId;

    /**
     * 编号方式: AUTO/MANUAL
     */

    @TableField(condition = SqlCondition.LIKE)
    private String noGenerateMode;

    /**
     * 进入定稿时间
     */
    private Date finalizedAt;

    /**
     * 进入定稿待报时间
     */
    private Date pendingReportAt;

    /**
     * 是否已同步申请专利表: 0否 1是
     */
    private Integer syncedToPatent;

    /**
     * 同步后的patent_new_application.id
     */
    private Long patentApplicationId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
