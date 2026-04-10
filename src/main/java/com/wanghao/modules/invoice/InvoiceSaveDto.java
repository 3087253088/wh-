package com.wanghao.modules.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class InvoiceSaveDto {
    @NotBlank(message = "发票号码不能为空")
    private String invoiceCode;

    @NotBlank(message = "发票类型不能为空")
    private String invoiceType;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    @NotBlank(message = "开票方不能为空")
    private String drawer;

    private String drawerTaxNo;

    @NotNull(message = "开票日期不能为空")
    private Date invoiceDate;

    private String imageUrl;
}
