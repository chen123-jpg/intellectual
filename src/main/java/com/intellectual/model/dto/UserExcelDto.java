package com.intellectual.model.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class UserExcelDto {
    @ExcelProperty("用户名")
    private String loginName;
    @ExcelProperty("手机号")
    private String phoneNumber;
    @ExcelProperty("邮箱")
    private String email;
    @ExcelProperty("角色")
    private String roleName;
}
