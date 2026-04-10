package com.wanghao.modules.invoice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("invoice")
public class Invoice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String invoiceCode;
    private String invoiceType;
    private BigDecimal amount;
    private String drawer;
    private String drawerTaxNo;
    private Date invoiceDate;
    private Long userId;
    private Long claimId;
    private String imageUrl;
    private String verifyStatus;
    private Date verifyTime;
    private String verifyResult;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
