package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.domain.Invoice;

/**
 * 发票申请 服务层
 *
 * @author ruoyi
 */
public interface IInvoiceService
{
    public List<Invoice> selectInvoiceList(Invoice invoice);

    public Invoice selectInvoiceById(Long invoiceId);

    public int insertInvoice(Invoice invoice);

    public int updateInvoice(Invoice invoice);

    public int deleteInvoiceByIds(Long[] invoiceIds);

    /**
     * 机构处理开票申请（status=1 已开票，status=2 驳回）
     */
    public int handleInvoice(Long invoiceId, String status, String invoiceNo, String remark);

    // ====================== 家属端 ======================

    public List<Invoice> selectFamilyInvoices(FamilyToken family, Long elderId);

    public Invoice createFamilyInvoice(FamilyToken family, Invoice invoice);
}
