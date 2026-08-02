package com.intellectual.websocket.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.intellectual.mapper.NotificationMessageMapper;
import com.intellectual.model.entity.NotificationMessage;
import com.intellectual.websocket.SimpleWsHandler;
import com.intellectual.websocket.WsOnlineReceiver;
import com.intellectual.websocket.WsPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushScheduler {

    private static final String LOCK_KEY = "push:unread:lock";
    private static final int LOCK_TTL = 60;
    private static final int BATCH_SIZE = 200;

    private final RedisTemplate<String, Object> redisTemplate;
    private final NotificationMessageMapper notificationMessageMapper;
    private final WsPublisher wsPublisher;
    private final WsOnlineReceiver wsOnlineReceiver;

    @Scheduled(initialDelay = 5_000, fixedDelay = 10_000)
    public void pushUnreadMessages() {
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(LOCK_KEY, "1", LOCK_TTL, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            log.debug("其他节点正在执行推送任务，跳过");
            return;
        }
        try {
            doPush();
        } finally {
            redisTemplate.delete(LOCK_KEY);
        }
    }

    private void doPush() {
        Date now = new Date();
        Long lastId = 0L;
        int totalPushed = 0;
        int totalScanned = 0;

        while (true) {
            List<NotificationMessage> messages = notificationMessageMapper.selectList(
                    new LambdaQueryWrapper<NotificationMessage>()
                            .eq(NotificationMessage::getIsPushed, 0)
                            .le(NotificationMessage::getPlannedSendTime, now)
                            .gt(NotificationMessage::getId, lastId)
                            .orderByAsc(NotificationMessage::getId)
                            .last("limit " + BATCH_SIZE)
            );
            if (messages.isEmpty()) {
                break;
            }
            totalScanned += messages.size();

            for (NotificationMessage msg : messages) {
                if (tryClaimAndPush(msg)) {
                    totalPushed++;
                }
            }
            lastId = messages.get(messages.size() - 1).getId();
        }

        if (totalScanned > 0) {
            log.info("本轮推送完成: {}/{}", totalPushed, totalScanned);
        }
    }

    private boolean tryClaimAndPush(NotificationMessage msg) {
        Long userId = msg.getUserId();
        boolean localOnline = SimpleWsHandler.LOCAL_SESSIONS.containsKey(userId);
        boolean globalOnline = wsOnlineReceiver.isOnline(userId);

        if (!localOnline && !globalOnline) {
            log.info("用户 {} 不在线(本地={},全局={})，消息 {} 暂存等待重试",
                    userId, localOnline, globalOnline, msg.getId());
            return false;
        }

        if (!globalOnline) {
            log.info("用户 {} 仅在本地在线，Redis Set 缺失，已修复", userId);
            wsOnlineReceiver.addOnline(userId);
        }

        Date now = new Date();
        LambdaUpdateWrapper<NotificationMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(NotificationMessage::getId, msg.getId())
                .eq(NotificationMessage::getIsPushed, 0)
                .set(NotificationMessage::getIsPushed, 1)
                .set(NotificationMessage::getActualSendTime, now);

        int updated = notificationMessageMapper.update(null, updateWrapper);
        if (updated == 0) {
            log.debug("消息 {} 已被其他节点推送，跳过", msg.getId());
            return false;
        }

        String json = TextJson.buildJson(msg);
        wsPublisher.sendToUser(msg.getUserId(), json);
        log.info("消息 {} 已推送至用户 {}", msg.getId(), msg.getUserId());
        return true;
    }
}
