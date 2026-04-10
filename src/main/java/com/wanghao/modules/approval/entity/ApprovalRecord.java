package com.wanghao.modules.approval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("approval_record")
public class ApprovalRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long claimId;
    private Long approverId;
    private String approverName;
    private String result;
    private String comment;
    private Date approvalTime;
    private Integer approvalOrder;
}
