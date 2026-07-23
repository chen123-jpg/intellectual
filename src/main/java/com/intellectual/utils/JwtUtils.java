package com.intellectual.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类 —— 负责 Token 的生成、解析与校验
 * <p>使用 HMAC-SHA256 算法签名，密钥与过期时间从配置文件注入</p>
 */
@Component
public class JwtUtils {

    /** HMAC 签名密钥，由配置文件中的 jwt.secret 初始化 */
    private final SecretKey secretKey;

    /** Token 过期时间（毫秒），默认 24 小时 */
    @Value("${jwt.expiration:86400000}")
    private long expiration;

    public JwtUtils(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token（带额外声明）
     *
     * @param userId      用户 ID，存入 subject 字段
     * @param loginName   登录账号，存入自定义声明
     * @param extraClaims 额外的自定义声明
     * @return JWT 字符串
     */
    public String generateToken(Long userId, String loginName, Map<String, Object> extraClaims) {
        Date now = new Date();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userId.toString())
                .claim("loginName", loginName)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 生成 JWT Token（不含额外声明）
     */
    public String generateToken(Long userId, String loginName) {
        return generateToken(userId, loginName, Map.of());
    }

    /**
     * 解析 Token 并返回 Claims 体
     * <p>若 Token 无效、过期或签名不匹配会抛出异常</p>
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 从 Token 中提取用户 ID */
    public Long getUserId(String token) {
        String subject = parseToken(token).getSubject();
        return Long.valueOf(subject);
    }

    /** 从 Token 中提取登录账号 */
    public String getLoginName(String token) {
        return parseToken(token).get("loginName", String.class);
    }

    /** 判断 Token 是否已过期 */
    public boolean isTokenExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }

    /**
     * 校验 Token 是否合法
     * <p>同时验证签名有效性与是否过期</p>
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
