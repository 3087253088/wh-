package com.wanghao.modules.approval;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanghao.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/approval")
@Tag(name = "审批管理", description = "审批相关操作")
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    @GetMapping("/pending")
    @Operation(summary = "获取待审批列表")
    public Result<Page<Map<String, Object>>> getPendingList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        return Result.success(approvalService.getPendingList(pageNum, pageSize, userId, role));
    }

    @GetMapping("/history")
    @Operation(summary = "获取已审批列表")
    public Result<Page<Map<String, Object>>> getHistoryList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(approvalService.getHistoryList(pageNum, pageSize, userId));
    }

    @PostMapping("/{taskId}")
    @Operation(summary = "审批操作")
    public Result<Void> approve(@PathVariable Long taskId,
                                @Valid @RequestBody ApprovalDto approvalDto,
                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String userName = (String) request.getAttribute("username");
        approvalService.approve(taskId, userId, userName, approvalDto);
        return Result.success();
    }
}
