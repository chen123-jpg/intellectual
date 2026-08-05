package com.intellectual.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 用户信息表
 *
 * @author 陈创
 * @since 2026-07-21 17:19
 */
@Data
@TableName("sys_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 关联员工ID
     */
    private Long empId;

    /**
     * 关联客户ID
     */
    private Long customerId;

    /**
     * 角色ID（主角色）
     */
    private Long roleId;

    /**
     * 登录账号(手机号)
     */
    private String loginName;

    /**
     * 显示昵称
     */
    private String userName;

    /**
     * 账号类型（INTERNAL/EXTERNAL_ADMIN/EXTERNAL_USER）
     */
    private String userType;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phoneNumber;

    /**
     * 用户性别（0男 1女 2未知）
     */
    private String sex;

    /**
     * 头像路径
     */
    private String avatar;

    /**
     * 密码
     */
    private String password;

    /**
     * 盐加密（BCrypt 已内置，此字段仅用于兼容旧逻辑，不映射数据库）
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String salt;

    /**
     * 账号状态（0正常 1停用）
     */
    private String status;

    /**
     * 账号有效期开始
     */
    private Date accountValidFrom;

    /**
     * 账号有效期结束
     */
    private Date accountValidTo;

    /**
     * 手机号是否验证（0未验证 1已验证）
     */
    private String phoneVerified;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    private Date loginDate;

    /**
     * 登录失败次数
     */
    private Integer loginFailCount;

    /**
     * 密码最后更新时间
     */
    private Date pwdUpdateDate;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;
}
