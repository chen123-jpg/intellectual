package com.intellectual.model.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UserVo {
    private Long userId;
    private Long deptId;
    private String loginName;
    private String userName;
    private String userType;
    private String email;
    private String phoneNumber;
    private String sex;
    private String status;
    private Date createTime;
    private List<Long> roleIds;
    private String remark;
}
