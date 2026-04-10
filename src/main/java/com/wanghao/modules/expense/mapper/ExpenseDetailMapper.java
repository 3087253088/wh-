package com.wanghao.modules.expense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanghao.modules.expense.entity.ExpenseDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
@Mapper
public interface ExpenseDetailMapper extends BaseMapper<ExpenseDetail> {

    @Select("SELECT * FROM smart_finance.expense_detail WHERE claim_id = #{claimId}")
    List<ExpenseDetail> selectByClaimId(@Param("claimId") Long claimId);

    @Delete("DELETE FROM smart_finance.expense_detail WHERE claim_id = #{claimId}")
    int deleteByClaimId(@Param("claimId") Long claimId);
}
