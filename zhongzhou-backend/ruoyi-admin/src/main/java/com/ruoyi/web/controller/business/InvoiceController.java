package com.ruoyi.web.controller.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.Invoice;
import com.ruoyi.business.service.IInvoiceService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 发票申请管理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/invoice")
public class InvoiceController extends BaseController
{
    @Autowired
    private IInvoiceService invoiceService;

    @PreAuthorize("@ss.hasPermi('business:invoice:list')")
    @GetMapping("/list")
    public TableDataInfo list(Invoice invoice)
    {
        startPage();
        List<Invoice> list = invoiceService.selectInvoiceList(invoice);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:invoice:list')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(invoiceService.selectInvoiceById(id));
    }

    /**
     * 开票处理（status=1 已开票，status=2 驳回）
     */
    @PreAuthorize("@ss.hasPermi('business:invoice:handle')")
    @Log(title = "发票申请", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult handle(@RequestBody Invoice invoice)
    {
        return toAjax(invoiceService.handleInvoice(invoice.getInvoiceId(), invoice.getStatus(),
                invoice.getInvoiceNo(), invoice.getRemark()));
    }

    @PreAuthorize("@ss.hasPermi('business:invoice:remove')")
    @Log(title = "发票申请", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(invoiceService.deleteInvoiceByIds(ids));
    }
}
