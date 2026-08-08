package com.intellectual.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MailRequest {
    //交底Id
    private Long disclosureId;
    /** 关联ID：交底ID或内部编号(P表关联键)，优先于 disclosureId/internalNo */
    private String referenceId;
    /** 内部编号（P表关联键） */
    private String internalNo;
    /** 收件人，逗号/分号分隔 */
    private String to;
    /** 抄送，逗号/分号分隔 */
    private String cc;
    /** 密送，逗号/分号分隔 */
    private String bcc;
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
    /** 关联的交底附件ID列表，与attachmentUrls按索引对应，可空 */
    private List<Long> disclosureAttachmentIds;
}