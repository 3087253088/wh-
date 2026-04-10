package com.wanghao.modules.expense;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanghao.common.result.Result;
import com.wanghao.modules.expense.ExpenseQueryDto;
import com.wanghao.modules.expense.ExpenseSaveDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/expense")
@Tag(name = "报销管理", description = "报销单的增删改查")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/list")
    @Operation(summary = "获取报销列表")
    public Result<Page<Map<String, Object>>> getList(ExpenseQueryDto queryDto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        return Result.success(expenseService.getList(queryDto, userId, role));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取报销详情")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        return Result.success(expenseService.getDetail(id, userId, role));
    }

    @PostMapping
    @Operation(summary = "新增报销（草稿）")
    public Result<Map<String, Long>> save(@Valid @RequestBody ExpenseSaveDto saveDto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long deptId = (Long) request.getAttribute("deptId");
        return Result.success(expenseService.save(saveDto, userId, deptId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改报销（草稿状态）")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ExpenseSaveDto saveDto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        saveDto.setId(id);
        expenseService.update(saveDto, userId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除报销（草稿状态）")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        expenseService.delete(id, userId);
        return Result.success();
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "提交报销（进入审批）")
    public Result<Void> submit(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        expenseService.submit(id, userId);
        return Result.success();
    }

    @PostMapping("/{id}/recall")
    @Operation(summary = "撤回报销")
    public Result<Void> recall(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        expenseService.recall(id, userId);
        return Result.success();
    }
}
