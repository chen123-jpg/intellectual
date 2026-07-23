package com.intellectual.model.entity;

import java.io.Serializable;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 用户邮箱表
 *
 * @author 陈创
 * @since 2026-07-23 19:09
 */
@Data
@TableName("mail")
public class Mail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 邮箱（登录账号）
     */
    private String email;

    /**
     * 邮箱SMTP授权码
     */
    private String authCode;

    /**
     * 自定义SMTP服务器（可选）
     */
    private String smtpHost;

    /**
     * 自定义SMTP端口（可选）
     */
    private Integer smtpPort;
}
