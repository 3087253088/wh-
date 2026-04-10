package com.wanghao.modules.expense.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("expense_detail")
public class ExpenseDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long claimId;
    private String expenseType;
    private BigDecimal amount;
    private String invoiceNo;
    private String description;
    private Date expenseDate;
}
