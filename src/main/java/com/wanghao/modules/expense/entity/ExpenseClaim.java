package com.wanghao.modules.expense.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("expense_claim")
public class ExpenseClaim {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String claimNo;
    private Long userId;
    private Long deptId;
    private String title;
    private BigDecimal totalAmount;
    private String status;
    private Date expenseDate;
    private String description;
    private Long currentApproverId;
    private Date submitTime;
    private Date approveTime;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
