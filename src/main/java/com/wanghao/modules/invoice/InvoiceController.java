package com.wanghao.modules.invoice;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanghao.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoice")
@Tag(name = "发票管理", description = "发票的增删改查")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/list")
    @Operation(summary = "获取我的发票列表")
    public Result<Page<Map<String, Object>>> getMyList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String invoiceCode,
            @RequestParam(required = false) String verifyStatus,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(invoiceService.getMyList(pageNum, pageSize, userId, invoiceCode, verifyStatus));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有发票（财务专用）")
    public Result<Page<Map<String, Object>>> getAllList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String invoiceCode,
            @RequestParam(required = false) String drawer,
            @RequestParam(required = false) String verifyStatus,
            @RequestParam(required = false) String userName,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role) && !"finance".equals(role)) {
            return Result.forbidden();
        }
        return Result.success(invoiceService.getAllList(pageNum, pageSize, invoiceCode, drawer, verifyStatus, userName));
    }

    @PostMapping
    @Operation(summary = "录入发票")
    public Result<Map<String, Long>> save(@Valid @RequestBody com.wanghao.modules.invoice.dto.InvoiceSaveDto saveDto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(invoiceService.save(saveDto, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除发票")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        invoiceService.delete(id, userId, role);
        return Result.success();
    }

    @PostMapping("/{id}/verify")
    @Operation(summary = "发票验真")
    public Result<Map<String, String>> verify(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(invoiceService.verify(id, userId));
    }

    @PostMapping("/batch-verify")
    @Operation(summary = "批量验真（财务专用）")
    public Result<Map<String, Object>> batchVerify(@RequestBody Map<String, List<Long>> params, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role) && !"finance".equals(role)) {
            return Result.forbidden();
        }
        return Result.success(invoiceService.batchVerify(params.get("ids")));
    }
}
