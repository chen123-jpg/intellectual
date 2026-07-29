package com.intellectual.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 新增、调整交底归属时使用的最小主办人信息。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SponsorOptionVo {
    private Long userId;
    private String userName;
    private String loginName;
}
