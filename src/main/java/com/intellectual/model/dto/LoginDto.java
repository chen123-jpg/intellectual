package com.intellectual.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginDto {

    @NotBlank(message = "登录类型不能为空")
    private String loginType;

    private String loginName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式错误")
    private String phoneNumber;

    @Size(min = 6,max = 6,message = "验证码长度为6位")
    private String smsCode;

    @Size(min = 6, max = 20, message = "密码长度6~20位")
    private String password;

    private String checkCodeKey;

    @NotBlank(message = "验证码不能为空")
    private String checkCode;
}
