package com.wanghao.modules.dashboard;

import com.wanghao.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "工作台", description = "工作台统计数据")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/statistics")
    @Operation(summary = "获取统计数据")
    public Result<Map<String, Object>> getStatistics(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        return Result.success(dashboardService.getStatistics(userId, role));
    }

    @GetMapping("/todos")
    @Operation(summary = "获取待办事项")
    public Result<List<Map<String, Object>>> getTodos(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        return Result.success(dashboardService.getTodos(userId, role));
    }

    @GetMapping("/expense-trend")
    @Operation(summary = "获取费用趋势")
    public Result<Map<String, Object>> getExpenseTrend(@RequestParam(defaultValue = "6") Integer months, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        return Result.success(dashboardService.getExpenseTrend(months, userId, role));
    }
}
