package com.intellectual.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 专利交底修改请求参数。
 *
 * <p>专利类型仅在修改交底时必填，新增交底使用 {@link PatentDisclosureDTO}，
 * 不接收也不校验专利类型。</p>
 */
public class PatentDisclosureUpdateDTO extends PatentDisclosureDTO {

    @Override
    @NotBlank(message = "专利类型不能为空")
    public String getPatentType() {
        return super.getPatentType();
    }
}
