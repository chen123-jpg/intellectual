package com.intellectual.config;

import com.intellectual.websocket.WsOnlineReceiver;
import com.intellectual.websocket.WsPushReceiver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    // 消息推送频道
    public static final String TOPIC_WS_PUSH = "ws:push";
    // 用户上下线频道
    public static final String TOPIC_WS_ONLINE = "ws:online";

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    // 消息监听容器
    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory factory,
                                                        MessageListenerAdapter pushAdapter,
                                                        MessageListenerAdapter onlineAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(pushAdapter, new ChannelTopic(TOPIC_WS_PUSH));
        container.addMessageListener(onlineAdapter, new ChannelTopic(TOPIC_WS_ONLINE));
        return container;
    }

    @Bean
    public MessageListenerAdapter pushAdapter(WsPushReceiver receiver) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(receiver, "onPushMessage");
        adapter.setSerializer(RedisSerializer.string());
        return adapter;
    }

    @Bean
    public MessageListenerAdapter onlineAdapter(WsOnlineReceiver receiver) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(receiver, "onOnlineEvent");
        adapter.setSerializer(RedisSerializer.string());
        return adapter;
    }
}
