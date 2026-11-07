package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.Invoice;

/**
 * 发票申请 数据层
 *
 * @author ruoyi
 */
public interface InvoiceMapper
{
    public List<Invoice> selectInvoiceList(Invoice invoice);

    public Invoice selectInvoiceById(Long invoiceId);

    public int insertInvoice(Invoice invoice);

    public int updateInvoice(Invoice invoice);

    public int deleteInvoiceByIds(Long[] invoiceIds);
}
