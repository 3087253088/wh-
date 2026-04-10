package com.wanghao.common.utils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtils {

    // 使用强度为10的BCrypt加密器
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(10);

    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        try {
            return ENCODER.matches(rawPassword, encodedPassword);
        } catch (Exception e) {
            System.err.println("密码匹配异常: " + e.getMessage());
            return false;
        }
    }
}