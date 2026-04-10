package com.wanghao.modules.auth.interceptor;

import com.wanghao.common.result.Result;
import com.wanghao.common.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            PrintWriter out = response.getWriter();
            out.write(new ObjectMapper().writeValueAsString(Result.unauthorized()));
            out.flush();
            return false;
        }

        token = token.substring(7);
        Claims claims = jwtUtils.parseToken(token);

        if (claims == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            PrintWriter out = response.getWriter();
            out.write(new ObjectMapper().writeValueAsString(Result.unauthorized()));
            out.flush();
            return false;
        }

        // 修改这里：将 String 转换为 Long 存入
        String userIdStr = claims.getSubject();
        Long userId = Long.valueOf(userIdStr);

        request.setAttribute("userId", userId);      // 存入 Long 类型
        request.setAttribute("username", claims.get("username"));
        request.setAttribute("role", claims.get("role"));

        return true;
    }
}