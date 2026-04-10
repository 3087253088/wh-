package com.wanghao.modules.invoice;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanghao.common.exception.BusinessException;
import com.wanghao.modules.invoice.dto.InvoiceSaveDto;
import com.wanghao.modules.invoice.entity.Invoice;
import com.wanghao.modules.invoice.mapper.InvoiceMapper;
import com.wanghao.modules.user.entity.SysUser;
import com.wanghao.modules.user.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceMapper invoiceMapper;

    @Autowired
    private SysUserMapper userMapper;

    public Page<Map<String, Object>> getMyList(Integer pageNum, Integer pageSize, Long userId, String invoiceCode, String verifyStatus) {
        Page<Invoice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getUserId, userId);
        wrapper.like(invoiceCode != null, Invoice::getInvoiceCode, invoiceCode);
        wrapper.eq(verifyStatus != null, Invoice::getVerifyStatus, verifyStatus);
        wrapper.orderByDesc(Invoice::getCreateTime);

        Page<Invoice> invoicePage = invoiceMapper.selectPage(page, wrapper);
        Page<Map<String, Object>> resultPage = new Page<>();
        resultPage.setCurrent(invoicePage.getCurrent());
        resultPage.setSize(invoicePage.getSize());
        resultPage.setTotal(invoicePage.getTotal());

        List<Map<String, Object>> records = invoicePage.getRecords().stream().map(invoice -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", invoice.getId());
            map.put("invoiceCode", invoice.getInvoiceCode());
            map.put("invoiceType", invoice.getInvoiceType());
            map.put("invoiceTypeText", getInvoiceTypeText(invoice.getInvoiceType()));
            map.put("amount", invoice.getAmount());
            map.put("drawer", invoice.getDrawer());
            map.put("invoiceDate", invoice.getInvoiceDate());
            map.put("verifyStatus", invoice.getVerifyStatus());
            map.put("verifyStatusText", getVerifyStatusText(invoice.getVerifyStatus()));
            map.put("createTime", invoice.getCreateTime());
            return map;
        }).collect(Collectors.toList());

        resultPage.setRecords(records);
        return resultPage;
    }

    public Page<Map<String, Object>> getAllList(Integer pageNum, Integer pageSize,
                                                String invoiceCode, String drawer,
                                                String verifyStatus, String userName) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);

        // 查询列表
        List<Map<String, Object>> list = invoiceMapper.selectAllWithUser(page, invoiceCode, drawer, verifyStatus, userName);

        // 查询总数
        Long total = invoiceMapper.countAllWithUser(invoiceCode, drawer, verifyStatus, userName);

        Page<Map<String, Object>> resultPage = new Page<>();
        resultPage.setCurrent(pageNum);
        resultPage.setSize(pageSize);
        resultPage.setTotal(total);
        resultPage.setRecords(list);

        return resultPage;
    }


    public Map<String, Long> save(InvoiceSaveDto saveDto, Long userId) {
        // 检查发票号是否已存在
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getInvoiceCode, saveDto.getInvoiceCode());
        if (invoiceMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("发票号码已存在，请勿重复录入");
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceCode(saveDto.getInvoiceCode());
        invoice.setInvoiceType(saveDto.getInvoiceType());
        invoice.setAmount(saveDto.getAmount());
        invoice.setDrawer(saveDto.getDrawer());
        invoice.setDrawerTaxNo(saveDto.getDrawerTaxNo());
        invoice.setInvoiceDate(saveDto.getInvoiceDate());
        invoice.setUserId(userId);
        invoice.setImageUrl(saveDto.getImageUrl());
        invoice.setVerifyStatus("unverified");

        invoiceMapper.insert(invoice);

        Map<String, Long> result = new HashMap<>();
        result.put("id", invoice.getId());
        return result;
    }

    public void delete(Long id, Long userId, String role) {
        Invoice invoice = invoiceMapper.selectById(id);
        if (invoice == null) {
            throw new BusinessException("发票不存在");
        }

        if (!"admin".equals(role) && !"finance".equals(role) && !invoice.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此发票");
        }

        if (invoice.getClaimId() != null) {
            throw new BusinessException("该发票已关联报销单，无法删除");
        }

        invoiceMapper.deleteById(id);
    }

    public Map<String, String> verify(Long id, Long userId) {
        Invoice invoice = invoiceMapper.selectById(id);
        if (invoice == null) {
            throw new BusinessException("发票不存在");
        }

        // TODO: 调用第三方验真API
        // 模拟验真逻辑
        boolean verified = true;

        if (verified) {
            invoice.setVerifyStatus("verified");
            invoice.setVerifyTime(new Date());
            invoice.setVerifyResult("发票信息一致，验真通过");
            invoiceMapper.updateById(invoice);

            Map<String, String> result = new HashMap<>();
            result.put("verifyStatus", "verified");
            result.put("verifyResult", "发票信息一致，验真通过");
            return result;
        } else {
            invoice.setVerifyStatus("failed");
            invoice.setVerifyTime(new Date());
            invoice.setVerifyResult("发票信息不一致，请核对");
            invoiceMapper.updateById(invoice);

            Map<String, String> result = new HashMap<>();
            result.put("verifyStatus", "failed");
            result.put("verifyResult", "发票信息不一致，请核对");
            return result;
        }
    }

    public Map<String, Object> batchVerify(List<Long> ids) {
        int success = 0;
        int failed = 0;
        List<Long> failedIds = new ArrayList<>();

        for (Long id : ids) {
            try {
                verify(id, null);
                success++;
            } catch (Exception e) {
                failed++;
                failedIds.add(id);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", ids.size());
        result.put("success", success);
        result.put("failed", failed);
        result.put("failedIds", failedIds);
        return result;
    }

    private String getInvoiceTypeText(String type) {
        Map<String, String> map = new HashMap<>();
        map.put("special", "增值税专用发票");
        map.put("normal", "增值税普通发票");
        map.put("electronic", "电子发票");
        map.put("other", "其他");
        return map.getOrDefault(type, type);
    }

    private String getVerifyStatusText(String status) {
        Map<String, String> map = new HashMap<>();
        map.put("unverified", "未验证");
        map.put("verified", "已验证");
        map.put("failed", "验真失败");
        return map.getOrDefault(status, status);
    }
}

