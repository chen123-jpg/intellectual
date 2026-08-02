package com.intellectual.websocket;

import com.alibaba.fastjson2.JSON;
import com.intellectual.model.dto.WsPushMsg;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WsPushReceiver {
    private final SimpleWsHandler wsHandler;

    public void onPushMessage(String json) {
        WsPushMsg pushMsg = JSON.parseObject(json, WsPushMsg.class);
        wsHandler.sendLocal(pushMsg.getUserId(), pushMsg.getContent());
    }
}
