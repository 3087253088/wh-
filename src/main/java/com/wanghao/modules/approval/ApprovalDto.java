package com.wanghao.modules.approval;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApprovalDto {
    @NotBlank(message = "审批结果不能为空")
    private String result;  // approved-通过，rejected-驳回

    private String comment;
}
