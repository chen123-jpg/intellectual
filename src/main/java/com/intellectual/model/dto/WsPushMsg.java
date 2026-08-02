package com.intellectual.model.dto;

import lombok.Data;

@Data
public class WsPushMsg {
    // 目标用户ID
    private Long userId;
    // 消息内容
    private String content;
}
