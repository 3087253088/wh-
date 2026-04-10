package com.wanghao.modules.auth;

import com.wanghao.common.result.Result;
import com.wanghao.modules.auth.LoginDto;
import com.wanghao.modules.auth.UserInfoVo;
import com.wanghao.modules.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "登录、登出、获取用户信息")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDto loginDto) {
        return Result.success(authService.login(loginDto));
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息")
    public Result<UserInfoVo> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(authService.getCurrentUser(userId));
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<Void> logout(HttpServletRequest request) {
        // 可选：将token加入黑名单
        return Result.success();
    }
}
