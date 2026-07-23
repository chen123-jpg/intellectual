package com.intellectual.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class MailSendDTO {
    private Long disclosureId;
    /** 模板编码，如 DISCLOSURE_CONTACT；可空则纯手工主题正文 */
    private String templateCode;
    private String toEmails;
    private String ccEmails;
    private String subject;
    private String content;
    /** 选用的交底附件 ID 列表（可增删） */
    private List<Long> attachmentIds;
    private Long senderUserId;
    private String senderName;
}
