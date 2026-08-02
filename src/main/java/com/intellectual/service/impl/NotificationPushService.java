package com.intellectual.service.impl;

import com.intellectual.mapper.NotificationMessageMapper;
import com.intellectual.model.entity.NotificationMessage;
import com.intellectual.websocket.WsOnlineReceiver;
import com.intellectual.websocket.WsPublisher;
import com.intellectual.websocket.utils.TextJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPushService {

    private final WsPublisher wsPublisher;
    private final WsOnlineReceiver wsOnlineReceiver;
    private final NotificationMessageMapper notificationMessageMapper;

    /**
     * 推送通知（业务入口）
     * @param userId 目标用户
     * @param title 标题
     * @param content 内容
     * @param link 跳转链接
     */
    @Transactional
    public void pushNotification(Long userId, String title, String content, String link) {
        // 1. 构建消息实体并先存入数据库（获取ID）
        NotificationMessage msg = new NotificationMessage();
        msg.setUserId(userId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setLink(link);
        msg.setCreateTime(new Date());
        msg.setPlannedSendTime(new Date()); // 定时任务依赖此字段，设为当前立即发送
        msg.setIsRead(0);

        // 2. 判断用户是否在线（查 Redis 集群状态）
        boolean online = wsOnlineReceiver.isOnline(userId);

        if (online) {
            // 在线：先更新状态为已推送，再通过 Redis 广播
            msg.setIsPushed(1);
            msg.setActualSendTime(new Date());
            notificationMessageMapper.insert(msg); // 入库

            // 构建推送负载并广播
            String jsonPayload = TextJson.buildJson(msg);
            wsPublisher.sendToUser(userId, jsonPayload);
            log.info("用户 {} 在线，实时推送消息 ID: {}", userId, msg.getId());
        } else {
            // 离线：存库，isPushed=0，等待定时任务轮询推送
            msg.setIsPushed(0);
            notificationMessageMapper.insert(msg);
            log.info("用户 {} 离线，消息 ID: {} 存入数据库待推送", userId, msg.getId());
        }
    }
}