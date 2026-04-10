package com.wanghao.modules.employee;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanghao.modules.employee.dto.EmployeeSaveDto ;
import com.wanghao.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/employee")
@Tag(name = "员工信息管理", description = "员工信息的增删改查")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/list")
    @Operation(summary = "获取员工列表")
    public Result<Page<Map<String, Object>>> getList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role) && !"finance".equals(role)) {
            return Result.forbidden();
        }
        return Result.success(employeeService.getList(pageNum, pageSize, name, deptId, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取员工详情")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role) && !"finance".equals(role)) {
            return Result.forbidden();
        }
        return Result.success(employeeService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增员工")
    public Result<Map<String, String>> save(@Valid @RequestBody EmployeeSaveDto saveDto, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role) && !"finance".equals(role)) {
            return Result.forbidden();
        }
        return Result.success(employeeService.save(saveDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑员工")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody EmployeeSaveDto saveDto, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role) && !"finance".equals(role)) {
            return Result.forbidden();
        }
        saveDto.setId(id);
        employeeService.update(saveDto);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "修改员工状态（离职/复职）")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> params, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role) && !"finance".equals(role)) {
            return Result.forbidden();
        }
        employeeService.updateStatus(id, params.get("status"));
        return Result.success();
    }
}
