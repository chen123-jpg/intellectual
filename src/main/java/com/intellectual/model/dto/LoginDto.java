package com.intellectual.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginDto {

    @NotBlank(message = "登录账号不能为空")
    @Size(min = 3, max = 30, message = "账号长度3~30位")
    private String loginName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式错误")
    private String phoneNumber;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6~20位")
    private String password;

    private String checkCodeKey;

    @NotBlank(message = "验证码不能为空")
    private String checkCode;
}
