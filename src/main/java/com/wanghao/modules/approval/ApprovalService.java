package com.wanghao.modules.approval;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanghao.common.exception.BusinessException;
import com.wanghao.modules.approval.ApprovalDto;
import com.wanghao.modules.approval.entity.ApprovalRecord;
import com.wanghao.modules.approval.mapper.ApprovalRecordMapper;
import com.wanghao.modules.expense.entity.ExpenseClaim;
import com.wanghao.modules.expense.mapper.ExpenseClaimMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApprovalService {

    @Autowired
    private ExpenseClaimMapper expenseClaimMapper;

    @Autowired
    private ApprovalRecordMapper approvalRecordMapper;

    public Page<Map<String, Object>> getPendingList(Integer pageNum, Integer pageSize, Long userId, String role) {
        Page<ExpenseClaim> page = new Page<>(pageNum, pageSize);

        List<ExpenseClaim> list;
        if ("admin".equals(role) || "finance".equals(role)) {
            list = expenseClaimMapper.selectPendingList(page, null);
        } else {
            list = expenseClaimMapper.selectPendingList(page, userId);
        }

        Page<Map<String, Object>> resultPage = new Page<>();
        resultPage.setCurrent(page.getCurrent());
        resultPage.setSize(page.getSize());

        List<Map<String, Object>> records = list.stream().map(claim -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", claim.getId());
            map.put("claimNo", claim.getClaimNo());
            map.put("title", claim.getTitle());
            map.put("totalAmount", claim.getTotalAmount());
            map.put("submitTime", claim.getSubmitTime());
            return map;
        }).toList();

        resultPage.setRecords(records);
        resultPage.setTotal(list.size());
        return resultPage;
    }

    public Page<Map<String, Object>> getHistoryList(Integer pageNum, Integer pageSize, Long userId) {
        Page<ApprovalRecord> page = new Page<>(pageNum, pageSize);
        List<ApprovalRecord> list = approvalRecordMapper.selectByApproverId(page, userId);

        Page<Map<String, Object>> resultPage = new Page<>();
        resultPage.setCurrent(page.getCurrent());
        resultPage.setSize(page.getSize());

        List<Map<String, Object>> records = list.stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("claimId", record.getClaimId());
            map.put("result", record.getResult());
            map.put("comment", record.getComment());
            map.put("approvalTime", record.getApprovalTime());
            return map;
        }).toList();

        resultPage.setRecords(records);
        resultPage.setTotal(page.getTotal());
        return resultPage;
    }

    @Transactional
    public void approve(Long taskId, Long userId, String userName, ApprovalDto approvalDto) {
        ExpenseClaim claim = expenseClaimMapper.selectById(taskId);
        if (claim == null) {
            throw new BusinessException("报销单不存在");
        }
        if (!"pending".equals(claim.getStatus())) {
            throw new BusinessException("该报销单已处理");
        }

        // 更新报销单状态
        if ("approved".equals(approvalDto.getResult())) {
            claim.setStatus("approved");
            claim.setApproveTime(new Date());
        } else {
            claim.setStatus("rejected");
        }
        expenseClaimMapper.updateById(claim);

        // 保存审批记录
        ApprovalRecord record = new ApprovalRecord();
        record.setClaimId(taskId);
        record.setApproverId(userId);
        record.setApproverName(userName);
        record.setResult(approvalDto.getResult());
        record.setComment(approvalDto.getComment());
        record.setApprovalTime(new Date());
        approvalRecordMapper.insert(record);

        // TODO: 发送通知给申请人
    }
}
