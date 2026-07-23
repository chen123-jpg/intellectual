package com.intellectual.config;

import com.intellectual.security.LoginUser;
import com.intellectual.security.UserDetailsServiceImpl;
import com.intellectual.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 *
 * <p>每个请求执行一次（继承 {@link OncePerRequestFilter}），流程：</p>
 * <ol>
 *   <li>从 {@code Authorization} 请求头提取 Bearer Token</li>
 *   <li>校验 Token 签名与有效期</li>
 *   <li>解析 userId，从数据库加载用户角色权限</li>
 *   <li>构造 {@link UsernamePasswordAuthenticationToken} 放入安全上下文</li>
 * </ol>
 *
 * <p>认证失败不阻断请求 —— 后续由 {@code SecurityFilterChain} 的 {@code .anyRequest().authenticated()}
 * 或 {@code @RequirePermission} 切面决定是否拒绝</p>
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null && jwtUtils.validateToken(token)) {
            try {
                // 从 JWT 中解析用户 ID，再从数据库加载完整的用户信息（含角色权限）
                Long userId = jwtUtils.getUserId(token);
                LoginUser loginUser = userDetailsService.loadUserById(userId);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.warn("JWT认证失败: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头提取 Bearer Token
     *
     * @return Token 字符串，若不存在或格式不正确返回 null
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
