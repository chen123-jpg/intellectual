package com.intellectual.model.dto;

import lombok.Data;

@Data
public class WsOnlineEvent {
    private Long userId;
    // online / offline
    private String action;
}
