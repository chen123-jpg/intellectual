package com.intellectual.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Date;

/**
 * 专利交底新增、修改请求参数。
 *
 * <p>只暴露允许客户端修改的业务字段。临时编号、录入人、复制来源、流程时间、
 * 同步状态及创建/更新时间等字段由服务端维护，不允许通过请求体写入。</p>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatentDisclosureDTO {

    /** 修改时必填；新增时即使传入也会被忽略。 */
    private Long id;

    private String internalNo;

    private String patentStatus;

    private String requirement;

    @NotBlank(message = "交底名称不能为空")
    private String disclosureName;

    private String applicant;

    private String inventor;

    private String patentType;

    @Min(value = 0, message = "是否邀请进群只能为0或1")
    @Max(value = 1, message = "是否邀请进群只能为0或1")
    private Integer invitedToGroup;

    private String contactPerson;

    private String manager;

    private String agent;

    private String mentor;

    private String businessPersonnel;

    private String sponsor;

    private Long sponsorUserId;

    private Date disclosureDate;

    private Integer disclosureDays;

    private String remark;

    private String contactInfo;

    private String contactEmail;

    private String contactPhone;

    @Pattern(regexp = "AUTO|MANUAL", message = "编号方式只能为AUTO或MANUAL")
    private String noGenerateMode;
}
