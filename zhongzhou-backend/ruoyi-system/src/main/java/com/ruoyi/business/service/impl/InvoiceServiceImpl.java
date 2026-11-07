package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.Bill;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.domain.Invoice;
import com.ruoyi.business.domain.vo.FamilyElderVO;
import com.ruoyi.business.mapper.BillMapper;
import com.ruoyi.business.mapper.FamilyMapper;
import com.ruoyi.business.mapper.InvoiceMapper;
import com.ruoyi.business.service.IInvoiceService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 发票申请 服务层处理
 *
 * @author ruoyi
 */
@Service
public class InvoiceServiceImpl implements IInvoiceService
{
    @Autowired
    private InvoiceMapper invoiceMapper;

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private FamilyMapper familyMapper;

    @Override
    public List<Invoice> selectInvoiceList(Invoice invoice)
    {
        return invoiceMapper.selectInvoiceList(invoice);
    }

    @Override
    public Invoice selectInvoiceById(Long invoiceId)
    {
        return invoiceMapper.selectInvoiceById(invoiceId);
    }

    @Override
    public int insertInvoice(Invoice invoice)
    {
        if (StringUtils.isNull(invoice.getTenantId()))
        {
            invoice.setTenantId(SecurityUtils.getTenantId());
        }
        if (StringUtils.isEmpty(invoice.getStatus()))
        {
            invoice.setStatus("0");
        }
        invoice.setCreateBy(SecurityUtils.getUsername());
        invoice.setCreateTime(DateUtils.getNowDate());
        return invoiceMapper.insertInvoice(invoice);
    }

    @Override
    public int updateInvoice(Invoice invoice)
    {
        invoice.setUpdateBy(SecurityUtils.getUsername());
        return invoiceMapper.updateInvoice(invoice);
    }

    @Override
    public int deleteInvoiceByIds(Long[] invoiceIds)
    {
        return invoiceMapper.deleteInvoiceByIds(invoiceIds);
    }

    @Override
    public int handleInvoice(Long invoiceId, String status, String invoiceNo, String remark)
    {
        Invoice invoice = invoiceMapper.selectInvoiceById(invoiceId);
        if (invoice == null)
        {
            throw new ServiceException("开票申请不存在");
        }
        if (!"1".equals(status) && !"2".equals(status))
        {
            throw new ServiceException("处理结果不正确");
        }
        if ("1".equals(status) && StringUtils.isEmpty(invoiceNo))
        {
            throw new ServiceException("请填写发票号码");
        }
        Invoice update = new Invoice();
        update.setInvoiceId(invoiceId);
        update.setStatus(status);
        update.setInvoiceNo(invoiceNo);
        update.setRemark(remark);
        update.setUpdateBy(SecurityUtils.getUsername());
        return invoiceMapper.updateInvoice(update);
    }

    // ====================== 家属端 ======================

    @Override
    public List<Invoice> selectFamilyInvoices(FamilyToken family, Long elderId)
    {
        checkElderOwnership(family, elderId);
        Invoice query = new Invoice();
        query.setElderId(elderId);
        query.setTenantId(family.getTenantId());
        return invoiceMapper.selectInvoiceList(query);
    }

    @Override
    public Invoice createFamilyInvoice(FamilyToken family, Invoice invoice)
    {
        if (invoice == null || invoice.getBillId() == null)
        {
            throw new ServiceException("请选择需要开票的账单");
        }
        if (StringUtils.isEmpty(invoice.getTitle()))
        {
            throw new ServiceException("请填写发票抬头");
        }
        Bill bill = billMapper.selectBillById(invoice.getBillId());
        if (bill == null || !family.getTenantId().equals(bill.getTenantId()))
        {
            throw new ServiceException("账单不存在");
        }
        FamilyElderVO elder = checkElderOwnership(family, bill.getElderId());

        Invoice duplicate = new Invoice();
        duplicate.setBillId(bill.getBillId());
        duplicate.setTenantId(family.getTenantId());
        List<Invoice> exists = invoiceMapper.selectInvoiceList(duplicate);
        if (exists != null && !exists.isEmpty())
        {
            throw new ServiceException("该账单已提交开票申请，请勿重复提交");
        }

        BigDecimal amount = bill.getPaidAmount() != null && bill.getPaidAmount().compareTo(BigDecimal.ZERO) > 0
                ? bill.getPaidAmount() : bill.getAmount();
        invoice.setBillNo(bill.getBillNo());
        invoice.setElderId(bill.getElderId());
        invoice.setElderName(elder.getElderName());
        invoice.setTenantId(family.getTenantId());
        invoice.setAmount(amount == null ? BigDecimal.ZERO : amount);
        invoice.setStatus("0");
        invoice.setCreateBy(family.getPhone());
        invoice.setCreateTime(DateUtils.getNowDate());
        invoiceMapper.insertInvoice(invoice);
        return invoice;
    }

    private FamilyElderVO checkElderOwnership(FamilyToken family, Long elderId)
    {
        if (elderId == null)
        {
            throw new ServiceException("请选择老人");
        }
        FamilyElderVO elder = familyMapper.selectElderDetail(family.getPhone(), family.getTenantId(), elderId);
        if (elder == null)
        {
            throw new ServiceException("无权查看该老人信息");
        }
        return elder;
    }
}
