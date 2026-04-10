package com.wanghao.modules.expense;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ExpenseSaveDto {
    private Long id;

    @NotBlank(message = "报销事由不能为空")
    private String title;

    @NotNull(message = "费用发生日期不能为空")
    private Date expenseDate;

    private String description;

    @Valid
    @NotNull(message = "报销明细不能为空")
    @Size(min = 1, message = "至少添加一条报销明细")
    private List<ExpenseDetailSaveDto> details;
}
