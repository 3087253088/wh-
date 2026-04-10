package com.wanghao.modules.budget;

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
@RequestMapping("/api/budget")
@Tag(name = "预算管理", description = "预算的增删改查")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @GetMapping("/list")
    @Operation(summary = "获取预算列表")
    public Result<Page<Map<String, Object>>> getList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String expenseType,
            @RequestParam(required = false) Integer year,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role) && !"finance".equals(role)) {
            return Result.forbidden();
        }
        return Result.success(budgetService.getList(pageNum, pageSize, deptId, expenseType, year));
    }

    @PostMapping
    @Operation(summary = "编制预算")
    public Result<Map<String, Long>> save(@Valid @RequestBody BudgetSaveDto saveDto, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role) && !"finance".equals(role)) {
            return Result.forbidden();
        }
        return Result.success(budgetService.save(saveDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "调整预算")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody BudgetSaveDto saveDto, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role) && !"finance".equals(role)) {
            return Result.forbidden();
        }
        saveDto.setId(id);
        budgetService.update(saveDto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除预算")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role) && !"finance".equals(role)) {
            return Result.forbidden();
        }
        budgetService.delete(id);
        return Result.success();
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取预算统计")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam Integer year,
            @RequestParam(required = false) Long deptId,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role) && !"finance".equals(role)) {
            return Result.forbidden();
        }
        return Result.success(budgetService.getStatistics(year, deptId));
    }
}
