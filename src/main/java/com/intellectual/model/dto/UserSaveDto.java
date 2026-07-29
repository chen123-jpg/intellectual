package com.intellectual.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserSaveDto {
    private Long userId;
    private Long deptId;
    private String loginName;
    private String userName;
    private String email;
    private String phoneNumber;
    private String sex;
    private String status;
    private String password;
    private List<Long> roleIds;
    private String remark;
}
