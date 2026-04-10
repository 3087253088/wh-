package com.wanghao.modules.budget.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("budget")
public class Budget {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deptId;
    private String expenseType;
    private Integer year;
    private Integer month;
    private BigDecimal budgetAmount;
    private BigDecimal usedAmount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
