package com.intellectual.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MailRequest {
    //交底Id
    private Long disclosureId;
    /** 收件人，逗号/分号分隔 */
    private String to;
    /** 抄送，逗号/分号分隔 */
    private String cc;
    /** 主题（若不使用模板则直接使用） */
    private String subject;
    /** 正文（若不使用模板则直接使用） */
    private String text;
    /** 模板编码，选用模板时传入 */
    private String templateCode;
    /** 模板变量，用于 Thymeleaf 渲染 */
    private Map<String, Object> templateData;
    /** 附件 URL 列表，来自 UploadFileController 返回的路径 */
    private List<String> attachmentUrls;
}