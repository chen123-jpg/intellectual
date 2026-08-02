package com.intellectual.websocket.utils;

import com.alibaba.fastjson2.JSON;
import com.intellectual.model.entity.NotificationMessage;

import java.util.HashMap;
import java.util.Map;

public class TextJson {
    public static String buildJson(NotificationMessage msg) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", msg.getId());
        data.put("title", msg.getTitle());
        data.put("content", msg.getContent());
        data.put("link", msg.getLink());
        data.put("isRead", msg.getIsRead());
        data.put("isPushed", msg.getIsPushed());
        data.put("plannedSendTime", msg.getPlannedSendTime());
        data.put("isEstimateCalc", msg.getIsEstimateCalc());
        data.put("type", "notification");
        return JSON.toJSONString(data);
    }

    public static NotificationMessage parsePayload(String json) {
        return JSON.parseObject(json, NotificationMessage.class);
    }
}
