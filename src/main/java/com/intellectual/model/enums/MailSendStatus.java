package com.intellectual.model.enums;

/**
 * 邮件发送状态枚举
 */
public enum MailSendStatus {

    PENDING(0, "待发送"),
    SUCCESS(1, "发送成功"),
    FAILED(2, "发送失败");

    private final int code;
    private final String description;

    MailSendStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据状态码获取对应的枚举实例
     * @param code 状态码
     * @return 枚举实例，如果未找到则返回 null
     */
    public static MailSendStatus fromCode(int code) {
        for (MailSendStatus status : MailSendStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据状态码获取枚举，若未找到则抛出异常（可选）
     */
    public static MailSendStatus fromCodeOrThrow(int code) {
        MailSendStatus status = fromCode(code);
        if (status == null) {
            throw new IllegalArgumentException("未知的邮件发送状态码: " + code);
        }
        return status;
    }

    @Override
    public String toString() {
        return String.format("%s(%d)", description, code);
    }
}
