package com.intellectual.websocket;

import com.alibaba.fastjson2.JSON;
import com.intellectual.mapper.NotificationMessageMapper;
import com.intellectual.model.dto.WsOnlineEvent;
import com.intellectual.model.dto.WsPushMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static com.intellectual.config.RedisConfig.TOPIC_WS_ONLINE;
import static com.intellectual.config.RedisConfig.TOPIC_WS_PUSH;

@Slf4j
@Component
@RequiredArgsConstructor
public class WsPublisher {

    /** Redis Set key，存储全局在线用户ID */
    public static final String WS_ONLINE_USERS_KEY = "ws:online:users";

    private final RedisTemplate<String, Object> redisTemplate;
    private final WsOnlineReceiver wsOnlineReceiver;
    private final NotificationMessageMapper notificationMessageMapper;

    /**
     * 集群推送消息给指定用户
     */
    public void sendToUser(Long userId, String content) {

        WsPushMsg pushMsg = new WsPushMsg();
        pushMsg.setUserId(userId);
        pushMsg.setContent(content);
        redisTemplate.convertAndSend(TOPIC_WS_PUSH, JSON.toJSONString(pushMsg));
    }

    /**
     * 发布上下线事件
     */
    public void publishOnlineEvent(WsOnlineEvent event) {
        String userIdStr = event.getUserId().toString();
        if ("online".equals(event.getAction())) {
            redisTemplate.opsForSet().add(WS_ONLINE_USERS_KEY, userIdStr);
            log.info("用户 {} 上线 (Redis Set 已添加)", userIdStr);
        } else if ("offline".equals(event.getAction())) {
            redisTemplate.opsForSet().remove(WS_ONLINE_USERS_KEY, userIdStr);
            log.info("用户 {} 下线 (Redis Set 已移除)", userIdStr);
        }
        redisTemplate.convertAndSend(TOPIC_WS_ONLINE, JSON.toJSONString(event));
    }
}
