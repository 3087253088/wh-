package com.wanghao.modules.profile;

import com.wanghao.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.wanghao.modules.profile.ProfileUpdateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Tag(name = "个人信息管理", description = "个人信息的查看和修改")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/profile")
    @Operation(summary = "获取个人信息")
    public Result<Map<String, Object>> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(profileService.getProfile(userId));
    }

    @PutMapping("/profile")
    @Operation(summary = "更新个人信息")
    public Result<Void> updateProfile(@Valid @RequestBody ProfileUpdateDto updateDto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        profileService.updateProfile(userId, updateDto);
        return Result.success();
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeDto changeDto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        profileService.changePassword(userId, changeDto);
        return Result.success();
    }

    @PostMapping("/avatar")
    @Operation(summary = "上传头像")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(profileService.uploadAvatar(userId, file));
    }
}
