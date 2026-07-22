package com.intellectual.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtils {
    
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public PasswordUtils() {
    }

    //加密密码
    public static String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    // 匹配
    // 明文  和   密文
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
