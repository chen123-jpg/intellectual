package com.intellectual.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum MailServerConfig {
    QQ("qq.com", "smtp.qq.com", 587),
    _163("163.com", "smtp.163.com", 465),
    _126("126.com", "smtp.126.com", 465),
    GMAIL("gmail.com", "smtp.gmail.com", 587),
    OUTLOOK("outlook.com", "smtp-mail.outlook.com", 587),
    HOTMAIL("hotmail.com", "smtp-mail.outlook.com", 587),
    YAHOO("yahoo.com", "smtp.mail.yahoo.com", 587),
    SINA("sina.com", "smtp.sina.com", 465),
    SOHU("sohu.com", "smtp.sohu.com", 465);

    private final String domain;       // 邮箱域名
    private final String host;         // SMTP 服务器
    private final int port;            // 端口

    MailServerConfig(String domain, String host, int port) {
        this.domain = domain;
        this.host = host;
        this.port = port;
    }

    // 根据邮箱地址获取配置
    public static MailServerConfig fromEmail(String email) {
        if (email == null || !email.contains("@")) return null;
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        for (MailServerConfig config : values()) {
            if (domain.equals(config.getDomain())) {
                return config;
            }
        }
        return null; // 或者抛出异常
    }
}
