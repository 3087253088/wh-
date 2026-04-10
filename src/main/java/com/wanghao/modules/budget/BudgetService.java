package com.wanghao.modules.budget;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanghao.common.exception.BusinessException;
import com.wanghao.modules.budget.entity.Budget;
import com.wanghao.modules.budget.mapper.BudgetMapper;
import com.wanghao.modules.dept.entity.SysDept;
import com.wanghao.modules.dept.mapper.SysDeptMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    @Autowired
    private BudgetMapper budgetMapper;

    @Autowired
    private SysDeptMapper deptMapper;

    public Page<Map<String, Object>> getList(Integer pageNum, Integer pageSize, Long deptId, String expenseType, Integer year) {
        Page<Budget> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Budget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(deptId != null, Budget::getDeptId, deptId);
        wrapper.eq(expenseType != null, Budget::getExpenseType, expenseType);
        wrapper.eq(year != null, Budget::getYear, year);
        wrapper.orderByDesc(Budget::getYear, Budget::getMonth);

        Page<Budget> budgetPage = budgetMapper.selectPage(page, wrapper);
        Page<Map<String, Object>> resultPage = new Page<>();
        resultPage.setCurrent(budgetPage.getCurrent());
        resultPage.setSize(budgetPage.getSize());
        resultPage.setTotal(budgetPage.getTotal());

        List<Map<String, Object>> records = budgetPage.getRecords().stream().map(budget -> {
            SysDept dept = deptMapper.selectById(budget.getDeptId());
            Map<String, Object> map = new HashMap<>();
            map.put("id", budget.getId());
            map.put("deptId", budget.getDeptId());
            map.put("deptName", dept != null ? dept.getDeptName() : "");
            map.put("expenseType", budget.getExpenseType());
            map.put("expenseTypeText", getExpenseTypeText(budget.getExpenseType()));
            map.put("year", budget.getYear());
            map.put("month", budget.getMonth());
            map.put("budgetAmount", budget.getBudgetAmount());
            map.put("usedAmount", budget.getUsedAmount());
            map.put("remainingAmount", budget.getBudgetAmount().subtract(budget.getUsedAmount()));
            if (budget.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0) {
                map.put("executionRate", budget.getUsedAmount().multiply(BigDecimal.valueOf(100))
                        .divide(budget.getBudgetAmount(), 0, BigDecimal.ROUND_HALF_UP));
            } else {
                map.put("executionRate", 0);
            }
            return map;
        }).collect(Collectors.toList());

        resultPage.setRecords(records);
        return resultPage;
    }

    public Map<String, Long> save(BudgetSaveDto saveDto) {
        // 检查唯一性
        LambdaQueryWrapper<Budget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Budget::getDeptId, saveDto.getDeptId())
                .eq(Budget::getExpenseType, saveDto.getExpenseType())
                .eq(Budget::getYear, saveDto.getYear())
                .eq(Budget::getMonth, saveDto.getMonth());
        if (budgetMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该预算已存在，请使用调整功能");
        }

        Budget budget = new Budget();
        budget.setDeptId(saveDto.getDeptId());
        budget.setExpenseType(saveDto.getExpenseType());
        budget.setYear(saveDto.getYear());
        budget.setMonth(saveDto.getMonth());
        budget.setBudgetAmount(saveDto.getBudgetAmount());
        budget.setUsedAmount(BigDecimal.ZERO);
        budget.setStatus(1);

        budgetMapper.insert(budget);

        Map<String, Long> result = new HashMap<>();
        result.put("id", budget.getId());
        return result;
    }

    public void update(BudgetSaveDto saveDto) {
        Budget budget = budgetMapper.selectById(saveDto.getId());
        if (budget == null) {
            throw new BusinessException("预算不存在");
        }

        budget.setBudgetAmount(saveDto.getBudgetAmount());
        budgetMapper.updateById(budget);
    }

    public void delete(Long id) {
        Budget budget = budgetMapper.selectById(id);
        if (budget == null) {
            throw new BusinessException("预算不存在");
        }
        budgetMapper.deleteById(id);
    }

    public Map<String, Object> getStatistics(Integer year, Long deptId) {
        Map<String, Object> result = new HashMap<>();

        // 获取所有预算
        LambdaQueryWrapper<Budget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Budget::getYear, year);
        wrapper.eq(deptId != null, Budget::getDeptId, deptId);
        List<Budget> budgets = budgetMapper.selectList(wrapper);

        // 总计
        BigDecimal totalBudget = budgets.stream().map(Budget::getBudgetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalUsed = budgets.stream().map(Budget::getUsedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        result.put("totalBudget", totalBudget);
        result.put("totalUsed", totalUsed);
        result.put("totalRemaining", totalBudget.subtract(totalUsed));

        // 按部门统计
        Map<Long, List<Budget>> byDept = budgets.stream().collect(Collectors.groupingBy(Budget::getDeptId));
        List<Map<String, Object>> byDeptList = new ArrayList<>();
        for (Map.Entry<Long, List<Budget>> entry : byDept.entrySet()) {
            SysDept dept = deptMapper.selectById(entry.getKey());
            BigDecimal deptBudget = entry.getValue().stream().map(Budget::getBudgetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal deptUsed = entry.getValue().stream().map(Budget::getUsedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> item = new HashMap<>();
            item.put("deptName", dept != null ? dept.getDeptName() : "");
            item.put("budgetAmount", deptBudget);
            item.put("usedAmount", deptUsed);
            item.put("remainingAmount", deptBudget.subtract(deptUsed));
            byDeptList.add(item);
        }
        result.put("byDept", byDeptList);

        // 按费用类型统计
        Map<String, List<Budget>> byType = budgets.stream().collect(Collectors.groupingBy(Budget::getExpenseType));
        List<Map<String, Object>> byTypeList = new ArrayList<>();
        for (Map.Entry<String, List<Budget>> entry : byType.entrySet()) {
            BigDecimal typeBudget = entry.getValue().stream().map(Budget::getBudgetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal typeUsed = entry.getValue().stream().map(Budget::getUsedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> item = new HashMap<>();
            item.put("expenseType", entry.getKey());
            item.put("expenseTypeText", getExpenseTypeText(entry.getKey()));
            item.put("budgetAmount", typeBudget);
            item.put("usedAmount", typeUsed);
            item.put("remainingAmount", typeBudget.subtract(typeUsed));
            byTypeList.add(item);
        }
        result.put("byExpenseType", byTypeList);

        return result;
    }

    private String getExpenseTypeText(String type) {
        Map<String, String> map = new HashMap<>();
        map.put("travel", "差旅费");
        map.put("entertainment", "业务招待费");
        map.put("office", "办公用品费");
        map.put("transportation", "交通费");
        map.put("training", "培训费");
        map.put("other", "其他");
        return map.getOrDefault(type, type);
    }
}
