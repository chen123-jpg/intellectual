package com.intellectual.websocket;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.intellectual.mapper.NotificationMessageMapper;
import com.intellectual.model.dto.WsOnlineEvent;
import com.intellectual.model.entity.NotificationMessage;
import com.intellectual.websocket.utils.TextJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SimpleWsHandler extends TextWebSocketHandler {

    public static final Map<Long, Set<WebSocketSession>> LOCAL_SESSIONS = new ConcurrentHashMap<>();

    private final WsPublisher wsPublisher;
    private final NotificationMessageMapper notificationMessageMapper;

    public SimpleWsHandler(WsPublisher wsPublisher,
                           NotificationMessageMapper notificationMessageMapper) {
        this.wsPublisher = wsPublisher;
        this.notificationMessageMapper = notificationMessageMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            session.close();
            return;
        }

        LOCAL_SESSIONS.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("用户{} 建立ws连接, sessionId:{}", userId, session.getId());

        WsOnlineEvent event = new WsOnlineEvent();
        event.setUserId(userId);
        event.setAction("online");
        wsPublisher.publishOnlineEvent(event);

        // 补推离线期间错过的未读消息
        // isPushed=1,isRead=0：调度器已推送但未送达
        // isPushed=0,plannedSendTime<=now：到期未推送
        Date now = new Date();
        List<NotificationMessage> offlineMsgs = notificationMessageMapper.selectList(
                new LambdaQueryWrapper<NotificationMessage>()
                        .eq(NotificationMessage::getUserId, userId)
                        .and(w -> w
                            .and(w1 -> w1.eq(NotificationMessage::getIsPushed, 1)
                                        .eq(NotificationMessage::getIsRead, 0))
                            .or(w2 -> w2.eq(NotificationMessage::getIsPushed, 0)
                                        .le(NotificationMessage::getPlannedSendTime, now)))
                        .orderByDesc(NotificationMessage::getPlannedSendTime)
                        .last("limit 10")
        );

        if (!offlineMsgs.isEmpty()) {
            for (NotificationMessage msg : offlineMsgs) {
                sendLocal(userId, TextJson.buildJson(msg));
                if (msg.getIsPushed() == 0) {
                    LambdaUpdateWrapper<NotificationMessage> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(NotificationMessage::getId, msg.getId())
                           .eq(NotificationMessage::getIsPushed, 0)
                           .set(NotificationMessage::getIsPushed, 1)
                           .set(NotificationMessage::getActualSendTime, now);
                    notificationMessageMapper.update(null, wrapper);
                }
            }
            log.info("用户{} 上线补推了 {} 条未读消息", userId, offlineMsgs.size());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            return;
        }
        Set<WebSocketSession> sessions = LOCAL_SESSIONS.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                LOCAL_SESSIONS.remove(userId);
                WsOnlineEvent event = new WsOnlineEvent();
                event.setUserId(userId);
                event.setAction("offline");
                wsPublisher.publishOnlineEvent(event);
            }
        }
        log.info("用户{} 断开ws连接", userId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("ws传输异常, sessionId:{}", session.getId(), exception);
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            Set<WebSocketSession> sessions = LOCAL_SESSIONS.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    LOCAL_SESSIONS.remove(userId);
                    WsOnlineEvent event = new WsOnlineEvent();
                    event.setUserId(userId);
                    event.setAction("offline");
                    wsPublisher.publishOnlineEvent(event);
                }
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            return;
        }
        String payload = message.getPayload();
        Map<String, Object> map;
        try {
            map = JSON.parseObject(payload);
        } catch (Exception e) {
            log.debug("非 JSON 消息，忽略");
            return;
        }

        if ("ping".equals(map.get("type"))) {
            Map<String, String> pong = new HashMap<>();
            pong.put("type", "pong");
            synchronized (session) {
                session.sendMessage(new TextMessage(JSON.toJSONString(pong)));
            }
            return;
        }

        if ("fetchUnread".equals(map.get("type"))) {
            Date now = new Date();
            List<NotificationMessage> unread = notificationMessageMapper.selectList(
                    new LambdaQueryWrapper<NotificationMessage>()
                            .eq(NotificationMessage::getUserId, userId)
                            .eq(NotificationMessage::getIsRead, 0)
                            .le(NotificationMessage::getPlannedSendTime, now)
                            .orderByDesc(NotificationMessage::getPlannedSendTime)
                            .last("limit 50")
            );
            Map<String, Object> resp = new HashMap<>();
            resp.put("type", "unreadList");
            resp.put("data", unread);
            synchronized (session) {
                session.sendMessage(new TextMessage(JSON.toJSONString(resp)));
            }
            return;
        }
    }

    public void sendLocal(Long userId, String content) {
        Set<WebSocketSession> sessions = LOCAL_SESSIONS.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        TextMessage msg = new TextMessage(content);
        Iterator<WebSocketSession> iterator = sessions.iterator();
        while (iterator.hasNext()) {
            WebSocketSession session = iterator.next();
            if (!session.isOpen()) {
                iterator.remove();
                try {
                    session.close();
                } catch (IOException ignored) {
                }
                continue;
            }
            try {
                synchronized (session) {
                    session.sendMessage(msg);
                }
            } catch (IllegalStateException | IOException e) {
                log.error("发送消息异常, sessionId:{}", session.getId(), e);
                iterator.remove();
                try {
                    session.close();
                } catch (IOException ignored) {
                }
            }
        }
        if (sessions.isEmpty()) {
            LOCAL_SESSIONS.remove(userId);
        }
    }
}
