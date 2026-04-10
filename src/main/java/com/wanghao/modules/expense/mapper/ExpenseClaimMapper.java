package com.wanghao.modules.expense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanghao.modules.expense.ExpenseQueryDto;
import com.wanghao.modules.expense.entity.ExpenseClaim;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
@Mapper
public interface ExpenseClaimMapper extends BaseMapper<ExpenseClaim> {

    List<ExpenseClaim> selectListByCondition(@Param("query") ExpenseQueryDto query, @Param("userId") Long userId);

    Long countByCondition(@Param("query") ExpenseQueryDto query, @Param("userId") Long userId);

    @Select("SELECT claim_no FROM smart_finance.expense_claim WHERE claim_no LIKE CONCAT('RE', #{date}, '%') ORDER BY id DESC LIMIT 1")
    String getMaxClaimNo(@Param("date") String date);

    /**
     * 查询待审批列表
     * @param page 分页对象
     * @param approverId 审批人ID（null表示查询所有）
     * @return 待审批报销单列表
     */
    List<ExpenseClaim> selectPendingList(Page<ExpenseClaim> page, @Param("approverId") Long approverId);
}
