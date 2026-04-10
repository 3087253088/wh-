package com.wanghao.modules.invoice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanghao.modules.invoice.entity.Invoice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface InvoiceMapper extends BaseMapper<Invoice> {

    /**
     * 分页查询所有发票（关联用户信息）
     */
    List<Map<String, Object>> selectAllWithUser(Page<?> page,
                                                @Param("invoiceCode") String invoiceCode,
                                                @Param("drawer") String drawer,
                                                @Param("verifyStatus") String verifyStatus,
                                                @Param("userName") String userName);

    /**
     * 统计所有发票数量（关联用户信息）
     */
    Long countAllWithUser(@Param("invoiceCode") String invoiceCode,
                          @Param("drawer") String drawer,
                          @Param("verifyStatus") String verifyStatus,
                          @Param("userName") String userName);
}