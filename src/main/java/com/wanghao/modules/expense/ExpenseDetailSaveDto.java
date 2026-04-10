package com.wanghao.modules.expense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ExpenseDetailSaveDto {
    @NotBlank(message = "费用类型不能为空")
    private String expenseType;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    private String invoiceNo;
    private String description;

    @NotNull(message = "费用日期不能为空")
    private Date expenseDate;
}
