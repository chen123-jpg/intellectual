package com.intellectual.config;

import com.intellectual.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 核心配置
 *
 * <p>设计要点：</p>
 * <ul>
 *   <li>无状态会话 —— 不使用 HttpSession，每次请求通过 JWT 认证</li>
 *   <li>白名单放行 —— /api/acount/checkCode、/api/acount/register、/api/acount/login 无需认证</li>
 *   <li>JWT 过滤器 —— 在 UsernamePasswordAuthenticationFilter 之前插入自定义过滤器</li>
 *   <li>BCrypt 密码编码 —— 所有密码使用 BCrypt 加密存储与比对</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          UserDetailsServiceImpl userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * 安全过滤链配置
     * <ul>
     *   <li>关闭 CSRF（前后端分离 + JWT 场景无需 CSRF 防护）</li>
     *   <li>无状态会话（STATEDELESS）</li>
     *   <li>白名单接口开放，其余请求需要认证</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/acount/checkCode",
                    "/api/acount/register",
                    "/api/acount/login",
                    "/api/acount/getSmsCode",
                    "/ws/**"
                ).permitAll()
                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                // 已上传文件（/files/**）允许匿名读取，浏览器 <img> 原生请求无法携带 JWT；
                // 上传(POST)与删除(DELETE)仍要求认证
                .requestMatchers(HttpMethod.GET, "/files/**", "/api/files/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** BCrypt 密码编码器 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     * <p>配置 DaoAuthenticationProvider：使用数据库用户详情 + BCrypt 密码比对</p>
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }
}
