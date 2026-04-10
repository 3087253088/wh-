package com.wanghao.modules.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanghao.modules.approval.entity.ApprovalRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecord> {

    @Select("SELECT * FROM smart_finance.approval_record WHERE approver_id = #{approverId} ORDER BY approval_time DESC")
    List<ApprovalRecord> selectByApproverId(Page<ApprovalRecord> page, @Param("approverId") Long approverId);
}
