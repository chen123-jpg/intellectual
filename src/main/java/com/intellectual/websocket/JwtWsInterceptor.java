package com.intellectual.websocket;

import com.intellectual.model.constants.Constants;
import com.intellectual.redis.RedisUtils;
import com.intellectual.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class JwtWsInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;

    public JwtWsInterceptor(JwtUtils jwtUtils, RedisUtils redisUtils) {
        this.jwtUtils = jwtUtils;
        this.redisUtils = redisUtils;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   org.springframework.web.socket.WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        ServletServerHttpRequest req = (ServletServerHttpRequest) request;
        HttpServletRequest servletRequest = req.getServletRequest();
        String token = servletRequest.getParameter("token");
        if (token == null || token.isEmpty()) {
            return false;
        }
        Long userId = parseToken(token);
        if (userId == null) {
            return false;
        }
        attributes.put("userId", userId);
        return true;
    }

    private Long parseToken(String token) {
        try {
            if (!jwtUtils.validateToken(token)) {
                return null;
            }
            Long userId = jwtUtils.getUserId(token);
            // 校验Redis中的token是否存在（退出登录会使Redis中的token失效）
            Object cachedToken = redisUtils.get(Constants.REDIS_KEY_JWT_TOKEN + userId);
            if (cachedToken == null) {
                return null;
            }
            return userId;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               org.springframework.web.socket.WebSocketHandler wsHandler, Exception exception) {
    }
}
