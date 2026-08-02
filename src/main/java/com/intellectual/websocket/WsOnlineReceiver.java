package com.intellectual.websocket;

import com.alibaba.fastjson2.JSON;
import com.intellectual.model.dto.WsOnlineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static com.intellectual.websocket.WsPublisher.WS_ONLINE_USERS_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class WsOnlineReceiver {

    private final RedisTemplate<String, Object> redisTemplate;

    /** 接收集群上下线事件，维护全局在线用户表 */
    public void onOnlineEvent(String json) {
        WsOnlineEvent event = JSON.parseObject(json, WsOnlineEvent.class);
        if ("online".equals(event.getAction())) {
            redisTemplate.opsForSet().add(WS_ONLINE_USERS_KEY, event.getUserId().toString());
            log.info("集群事件: 用户{} 上线", event.getUserId());
        } else if ("offline".equals(event.getAction())) {
            redisTemplate.opsForSet().remove(WS_ONLINE_USERS_KEY, event.getUserId().toString());
            log.info("集群事件: 用户{} 下线", event.getUserId());
        }
    }

    /** 查询用户是否在线 */
    public boolean isOnline(Long userId) {
        Boolean member = redisTemplate.opsForSet().isMember(WS_ONLINE_USERS_KEY, userId.toString());
        return Boolean.TRUE.equals(member);
    }

    /** 手动补加在线用户（用于修复丢失） */
    public void addOnline(Long userId) {
        redisTemplate.opsForSet().add(WS_ONLINE_USERS_KEY, userId.toString());
    }

    /** 获取在线用户数 */
    public long getOnlineCount() {
        Long size = redisTemplate.opsForSet().size(WS_ONLINE_USERS_KEY);
        return size != null ? size : 0;
    }
}
