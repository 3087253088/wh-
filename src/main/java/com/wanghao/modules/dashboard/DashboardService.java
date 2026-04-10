package com.wanghao.modules.dashboard;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanghao.modules.budget.entity.Budget;
import com.wanghao.modules.budget.mapper.BudgetMapper;
import com.wanghao.modules.expense.entity.ExpenseClaim;
import com.wanghao.modules.expense.mapper.ExpenseClaimMapper;
import com.wanghao.modules.invoice.entity.Invoice;
import com.wanghao.modules.invoice.mapper.InvoiceMapper;
import com.wanghao.modules.user.entity.SysUser;
import com.wanghao.modules.user.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private ExpenseClaimMapper expenseClaimMapper;

    @Autowired
    private BudgetMapper budgetMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private InvoiceMapper invoiceMapper;

    public Map<String, Object> getStatistics(Long userId, String role) {
        Map<String, Object> result = new HashMap<>();

        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        if ("admin".equals(role) || "finance".equals(role)) {
            // 本月报销总额
            LambdaQueryWrapper<ExpenseClaim> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.eq(ExpenseClaim::getStatus, "approved")
                    .apply("YEAR(submit_time) = {0}", currentYear)
                    .apply("MONTH(submit_time) = {0}", currentMonth);
            List<ExpenseClaim> approvedClaims = expenseClaimMapper.selectList(wrapper1);
            BigDecimal monthExpenseTotal = approvedClaims.stream()
                    .map(ExpenseClaim::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            result.put("monthExpenseTotal", monthExpenseTotal);

            // 待审批数
            LambdaQueryWrapper<ExpenseClaim> wrapper2 = new LambdaQueryWrapper<>();
            wrapper2.eq(ExpenseClaim::getStatus, "pending");
            Long pendingCount = expenseClaimMapper.selectCount(wrapper2);
            result.put("pendingApprovalCount", pendingCount);

            // 预算执行率
            LambdaQueryWrapper<Budget> wrapper3 = new LambdaQueryWrapper<>();
            wrapper3.eq(Budget::getYear, currentYear);
            List<Budget> budgets = budgetMapper.selectList(wrapper3);
            BigDecimal totalBudget = budgets.stream().map(Budget::getBudgetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalUsed = budgets.stream().map(Budget::getUsedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
                int budgetExecutionRate = totalUsed.multiply(BigDecimal.valueOf(100))
                        .divide(totalBudget, 0, RoundingMode.HALF_UP).intValue();
                result.put("budgetExecutionRate", budgetExecutionRate);
            } else {
                result.put("budgetExecutionRate", 0);
            }

            // 员工数量
            LambdaQueryWrapper<SysUser> wrapper4 = new LambdaQueryWrapper<>();
            wrapper4.eq(SysUser::getStatus, 1);
            Long employeeCount = userMapper.selectCount(wrapper4);
            result.put("employeeCount", employeeCount);

        } else {
            // 普通员工：个人报销总额
            LambdaQueryWrapper<ExpenseClaim> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.eq(ExpenseClaim::getUserId, userId)
                    .eq(ExpenseClaim::getStatus, "approved")
                    .apply("YEAR(submit_time) = {0}", currentYear)
                    .apply("MONTH(submit_time) = {0}", currentMonth);
            List<ExpenseClaim> approvedClaims = expenseClaimMapper.selectList(wrapper1);
            BigDecimal myExpenseTotal = approvedClaims.stream()
                    .map(ExpenseClaim::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            result.put("myExpenseTotal", myExpenseTotal);

            // 待审批数
            LambdaQueryWrapper<ExpenseClaim> wrapper2 = new LambdaQueryWrapper<>();
            wrapper2.eq(ExpenseClaim::getUserId, userId)
                    .eq(ExpenseClaim::getStatus, "pending");
            Long pendingCount = expenseClaimMapper.selectCount(wrapper2);
            result.put("pendingApprovalCount", pendingCount);

            // 我的发票数
            LambdaQueryWrapper<Invoice> wrapper3 = new LambdaQueryWrapper<>();
            wrapper3.eq(Invoice::getUserId, userId);
            Long invoiceCount = invoiceMapper.selectCount(wrapper3);
            result.put("myInvoiceCount", invoiceCount);
        }

        return result;
    }

    public List<Map<String, Object>> getTodos(Long userId, String role) {
        List<Map<String, Object>> todos = new ArrayList<>();

        if ("admin".equals(role) || "finance".equals(role)) {
            LambdaQueryWrapper<ExpenseClaim> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExpenseClaim::getStatus, "pending")
                    .orderByDesc(ExpenseClaim::getSubmitTime);
            List<ExpenseClaim> pendingList = expenseClaimMapper.selectList(wrapper);

            for (ExpenseClaim claim : pendingList) {
                SysUser user = userMapper.selectById(claim.getUserId());
                Map<String, Object> todo = new HashMap<>();
                todo.put("id", claim.getId());
                todo.put("type", "approval");
                todo.put("title", "待审批：" + claim.getTitle());
                todo.put("applicantName", user != null ? user.getName() : "");
                todo.put("amount", claim.getTotalAmount());
                todo.put("createTime", claim.getSubmitTime());
                todos.add(todo);
            }
        }

        return todos;
    }

    public Map<String, Object> getExpenseTrend(Integer months, Long userId, String role) {
        Map<String, Object> result = new HashMap<>();

        List<String> monthsList = new ArrayList<>();
        List<BigDecimal> amountsList = new ArrayList<>();

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = months - 1; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            String monthStr = date.format(formatter);
            monthsList.add(monthStr);

            LambdaQueryWrapper<ExpenseClaim> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExpenseClaim::getStatus, "approved")
                    .apply("YEAR(submit_time) = {0}", date.getYear())
                    .apply("MONTH(submit_time) = {0}", date.getMonthValue());

            if (!"admin".equals(role) && !"finance".equals(role)) {
                wrapper.eq(ExpenseClaim::getUserId, userId);
            }

            List<ExpenseClaim> claims = expenseClaimMapper.selectList(wrapper);
            BigDecimal total = claims.stream()
                    .map(ExpenseClaim::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            amountsList.add(total);
        }

        result.put("months", monthsList);
        result.put("amounts", amountsList);
        return result;
    }
}
