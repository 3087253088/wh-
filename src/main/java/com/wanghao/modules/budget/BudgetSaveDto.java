package com.wanghao.modules.budget;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetSaveDto {
    private Long id;

    @NotNull(message = "部门不能为空")
    private Long deptId;

    @NotNull(message = "费用类型不能为空")
    private String expenseType;

    @NotNull(message = "年份不能为空")
    private Integer year;

    @NotNull(message = "月份不能为空")
    private Integer month;

    @NotNull(message = "预算金额不能为空")
    @DecimalMin(value = "0.01", message = "预算金额必须大于0")
    private BigDecimal budgetAmount;
}
