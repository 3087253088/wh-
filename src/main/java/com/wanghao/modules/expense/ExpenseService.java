package com.wanghao.modules.expense;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanghao.common.exception.BusinessException;
import com.wanghao.common.utils.JwtUtils;
import com.wanghao.modules.expense.entity.ExpenseClaim;
import com.wanghao.modules.expense.entity.ExpenseDetail;
import com.wanghao.modules.expense.mapper.ExpenseClaimMapper;
import com.wanghao.modules.expense.mapper.ExpenseDetailMapper;
import com.wanghao.modules.user.mapper.SysUserMapper;
import com.wanghao.modules.user.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseClaimMapper expenseClaimMapper;

    @Autowired
    private ExpenseDetailMapper expenseDetailMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public Page<Map<String, Object>> getList(ExpenseQueryDto queryDto, Long userId, String role) {
        Page<ExpenseClaim> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        List<ExpenseClaim> list;

        if ("admin".equals(role) || "finance".equals(role)) {
            // 财务和管理员查看所有
            list = expenseClaimMapper.selectListByCondition(queryDto, null);
        } else {
            // 普通员工只查看自己的
            list = expenseClaimMapper.selectListByCondition(queryDto, userId);
        }

        Page<Map<String, Object>> resultPage = new Page<>();
        resultPage.setCurrent(page.getCurrent());
        resultPage.setSize(page.getSize());
        resultPage.setTotal(expenseClaimMapper.countByCondition(queryDto, userId));

        List<Map<String, Object>> records = list.stream().map(claim -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", claim.getId());
            map.put("claimNo", claim.getClaimNo());
            map.put("title", claim.getTitle());
            map.put("totalAmount", claim.getTotalAmount());
            map.put("status", claim.getStatus());
            map.put("statusText", getStatusText(claim.getStatus()));
            map.put("expenseDate", claim.getExpenseDate());
            map.put("submitTime", claim.getSubmitTime());
            return map;
        }).collect(Collectors.toList());

        resultPage.setRecords(records);
        return resultPage;
    }

    public Map<String, Object> getDetail(Long id, Long userId, String role) {
        ExpenseClaim claim = expenseClaimMapper.selectById(id);
        if (claim == null) {
            throw new BusinessException("报销单不存在");
        }

        // 权限验证
        if (!"admin".equals(role) && !"finance".equals(role) && !claim.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此报销单");
        }

        SysUser user = userMapper.selectById(claim.getUserId());
        List<ExpenseDetail> details = expenseDetailMapper.selectByClaimId(id);

        Map<String, Object> result = new HashMap<>();
        result.put("id", claim.getId());
        result.put("claimNo", claim.getClaimNo());
        result.put("userId", claim.getUserId());
        result.put("userName", user != null ? user.getName() : "");
        result.put("deptId", claim.getDeptId());
        result.put("title", claim.getTitle());
        result.put("totalAmount", claim.getTotalAmount());
        result.put("status", claim.getStatus());
        result.put("expenseDate", claim.getExpenseDate());
        result.put("description", claim.getDescription());
        result.put("details", details);
        result.put("createTime", claim.getCreateTime());
        result.put("submitTime", claim.getSubmitTime());
        result.put("approveTime", claim.getApproveTime());

        return result;
    }

    @Transactional
    public Map<String, Long> save(ExpenseSaveDto saveDto, Long userId, Long deptId) {
        ExpenseClaim claim = new ExpenseClaim();
        claim.setUserId(userId);
        claim.setDeptId(deptId);
        claim.setTitle(saveDto.getTitle());
        claim.setExpenseDate(saveDto.getExpenseDate());
        claim.setDescription(saveDto.getDescription());
        claim.setStatus("draft");

        // 计算总金额
        BigDecimal total = saveDto.getDetails().stream()
                .map(ExpenseDetailSaveDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        claim.setTotalAmount(total);

        // 生成单号
        String claimNo = generateClaimNo();
        claim.setClaimNo(claimNo);

        expenseClaimMapper.insert(claim);

        // 保存明细
        for (ExpenseDetailSaveDto detailDto : saveDto.getDetails()) {
            ExpenseDetail detail = new ExpenseDetail();
            detail.setClaimId(claim.getId());
            detail.setExpenseType(detailDto.getExpenseType());
            detail.setAmount(detailDto.getAmount());
            detail.setInvoiceNo(detailDto.getInvoiceNo());
            detail.setDescription(detailDto.getDescription());
            detail.setExpenseDate(detailDto.getExpenseDate());
            expenseDetailMapper.insert(detail);
        }

        Map<String, Long> result = new HashMap<>();
        result.put("id", claim.getId());
        return result;
    }

    @Transactional
    public void update(ExpenseSaveDto saveDto, Long userId) {
        ExpenseClaim claim = expenseClaimMapper.selectById(saveDto.getId());
        if (claim == null) {
            throw new BusinessException("报销单不存在");
        }
        if (!"draft".equals(claim.getStatus())) {
            throw new BusinessException("只有草稿状态的报销单可以修改");
        }
        if (!claim.getUserId().equals(userId)) {
            throw new BusinessException("只能修改自己的报销单");
        }

        claim.setTitle(saveDto.getTitle());
        claim.setExpenseDate(saveDto.getExpenseDate());
        claim.setDescription(saveDto.getDescription());

        BigDecimal total = saveDto.getDetails().stream()
                .map(ExpenseDetailSaveDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        claim.setTotalAmount(total);

        expenseClaimMapper.updateById(claim);

        // 删除旧明细，插入新明细
        expenseDetailMapper.deleteByClaimId(claim.getId());
        for (ExpenseDetailSaveDto detailDto : saveDto.getDetails()) {
            ExpenseDetail detail = new ExpenseDetail();
            detail.setClaimId(claim.getId());
            detail.setExpenseType(detailDto.getExpenseType());
            detail.setAmount(detailDto.getAmount());
            detail.setInvoiceNo(detailDto.getInvoiceNo());
            detail.setDescription(detailDto.getDescription());
            detail.setExpenseDate(detailDto.getExpenseDate());
            expenseDetailMapper.insert(detail);
        }
    }

    @Transactional
    public void delete(Long id, Long userId) {
        ExpenseClaim claim = expenseClaimMapper.selectById(id);
        if (claim == null) {
            throw new BusinessException("报销单不存在");
        }
        if (!"draft".equals(claim.getStatus())) {
            throw new BusinessException("只有草稿状态的报销单可以删除");
        }
        if (!claim.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的报销单");
        }

        expenseDetailMapper.deleteByClaimId(id);
        expenseClaimMapper.deleteById(id);
    }

    @Transactional
    public void submit(Long id, Long userId) {
        ExpenseClaim claim = expenseClaimMapper.selectById(id);
        if (claim == null) {
            throw new BusinessException("报销单不存在");
        }
        if (!"draft".equals(claim.getStatus())) {
            throw new BusinessException("只有草稿状态的报销单可以提交");
        }
        if (!claim.getUserId().equals(userId)) {
            throw new BusinessException("只能提交自己的报销单");
        }

        claim.setStatus("pending");
        claim.setSubmitTime(new Date());
        expenseClaimMapper.updateById(claim);

        // TODO: 创建审批任务，通知审批人
    }

    @Transactional
    public void recall(Long id, Long userId) {
        ExpenseClaim claim = expenseClaimMapper.selectById(id);
        if (claim == null) {
            throw new BusinessException("报销单不存在");
        }
        if (!"pending".equals(claim.getStatus())) {
            throw new BusinessException("只有审批中的报销单可以撤回");
        }
        if (!claim.getUserId().equals(userId)) {
            throw new BusinessException("只能撤回自己的报销单");
        }

        claim.setStatus("draft");
        claim.setSubmitTime(null);
        expenseClaimMapper.updateById(claim);
    }

    private String generateClaimNo() {
        String date = LocalDateTime.now().format(DATE_FORMAT);
        String maxNo = expenseClaimMapper.getMaxClaimNo(date);
        int seq = 1;
        if (maxNo != null) {
            seq = Integer.parseInt(maxNo.substring(11)) + 1;
        }
        return "RE" + date + String.format("%06d", seq);
    }

    private String getStatusText(String status) {
        Map<String, String> map = new HashMap<>();
        map.put("draft", "草稿");
        map.put("pending", "审批中");
        map.put("approved", "已通过");
        map.put("rejected", "已驳回");
        return map.getOrDefault(status, status);
    }
}
