package com.intellectual.config;

import com.intellectual.websocket.JwtWsInterceptor;
import com.intellectual.websocket.SimpleWsHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SimpleWsHandler simpleWsHandler;
    private final JwtWsInterceptor jwtWsInterceptor;

    public WebSocketConfig(SimpleWsHandler simpleWsHandler, JwtWsInterceptor jwtWsInterceptor) {
        this.simpleWsHandler = simpleWsHandler;
        this.jwtWsInterceptor = jwtWsInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(simpleWsHandler, "/ws")
                .addInterceptors(jwtWsInterceptor)
                .setAllowedOrigins("*");
    }
}
