package com.wanghao.modules.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanghao.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "系统用户的增删改查")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    @Operation(summary = "获取用户列表")
    public Result<Page<Map<String, Object>>> getList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.forbidden();
        }
        return Result.success(userService.getList(pageNum, pageSize, username, name, roleCode, status));
    }

    @PostMapping
    @Operation(summary = "新增用户")
    public Result<Map<String, Long>> save(@Valid @RequestBody UserSaveDto saveDto, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.forbidden();
        }
        return Result.success(userService.save(saveDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑用户")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserSaveDto saveDto, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.forbidden();
        }
        saveDto.setId(id);
        userService.update(saveDto);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "修改用户状态（启用/禁用）")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> params, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.forbidden();
        }
        userService.updateStatus(id, params.get("status"));
        return Result.success();
    }

    @PutMapping("/{id}/reset-password")
    @Operation(summary = "重置密码")
    public Result<Map<String, String>> resetPassword(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.forbidden();
        }
        return Result.success(userService.resetPassword(id));
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "分配角色")
    public Result<Void> assignRole(@PathVariable Long id, @RequestBody Map<String, String> params, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.forbidden();
        }
        userService.assignRole(id, params.get("roleCode"));
        return Result.success();
    }
}
