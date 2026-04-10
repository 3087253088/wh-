package com.wanghao.modules.expense;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class ExpenseQueryDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String title;
    private String status;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    public Integer getOffset() {
        if (pageNum != null && pageSize != null) {
            return (pageNum - 1) * pageSize;
        }
        return 0;
    }
}
