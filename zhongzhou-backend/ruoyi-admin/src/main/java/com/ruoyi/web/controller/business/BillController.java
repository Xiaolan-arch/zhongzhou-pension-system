package com.ruoyi.web.controller.business;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.Bill;
import com.ruoyi.business.domain.BillItem;
import com.ruoyi.business.domain.PaymentRecord;
import com.ruoyi.business.service.IBillService;
import com.ruoyi.business.service.IWeChatMessageService;
import com.ruoyi.business.util.InvoicePdfGenerator;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 账单 控制器
 */
@RestController
@RequestMapping("/business/bill")
public class BillController extends BaseController
{
    @Autowired
    private IBillService billService;

    @Autowired
    private IWeChatMessageService weChatMessageService;

    /** 查询账单列表 */
    @PreAuthorize("@ss.hasPermi('business:bill:list')")
    @GetMapping("/list")
    public TableDataInfo list(Bill bill)
    {
        startPage();
        List<Bill> list = billService.selectBillList(bill);
        return getDataTable(list);
    }

    /** 查询账单详情（含明细） */
    @PreAuthorize("@ss.hasPermi('business:bill:list')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        Bill bill = billService.selectBillById(id);
        List<BillItem> items = billService.selectBillItemsByBillId(id);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("bill", bill);
        data.put("items", items);
        return success(data);
    }

    /** 生成月度账单 */
    @PreAuthorize("@ss.hasPermi('business:bill:generate')")
    @Log(title = "账单管理", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    public AjaxResult generate(@RequestBody Bill bill)
    {
        int count = billService.generateBill(bill.getBillMonth());
        return success(count);
    }

    /** 账单缴费（支持部分收款） */
    @PreAuthorize("@ss.hasPermi('business:bill:pay')")
    @Log(title = "账单管理", businessType = BusinessType.UPDATE)
    @PutMapping("/pay")
    public AjaxResult pay(@RequestBody Bill bill)
    {
        return toAjax(billService.payBill(bill));
    }

    /** 账单退款（支持部分退款，前端传 billId / refundAmount / remark） */
    @PreAuthorize("@ss.hasPermi('business:bill:pay')")
    @Log(title = "账单管理", businessType = BusinessType.UPDATE)
    @PostMapping("/refund")
    public AjaxResult refund(@RequestBody Bill bill)
    {
        return toAjax(billService.refundBill(bill.getBillId(), bill.getRefundAmount(), bill.getRemark()));
    }

    /** 查询账单的收款记录 */
    @PreAuthorize("@ss.hasPermi('business:bill:list')")
    @GetMapping("/record/{billId}")
    public AjaxResult records(@PathVariable Long billId)
    {
        List<PaymentRecord> records = billService.selectPaymentRecords(billId);
        return success(records);
    }

    /** 查询逾期账单列表 */
    @PreAuthorize("@ss.hasPermi('business:bill:list')")
    @GetMapping("/overdue")
    public AjaxResult overdue()
    {
        Long tenantId = getTenantId();
        List<Bill> list = billService.selectOverdueBills(tenantId);
        return success(list);
    }

    /** 手动重算逾期账单滞纳金（返回更新条数） */
    @PreAuthorize("@ss.hasPermi('business:bill:penalty')")
    @Log(title = "账单管理", businessType = BusinessType.UPDATE)
    @PostMapping("/penalty/refresh")
    public AjaxResult refreshPenalty()
    {
        return success(billService.refreshOverduePenalty());
    }

    /** 向账单所属老人的家属发送微信提醒（默认 Mock 模式只落库发送记录） */
    @PreAuthorize("@ss.hasPermi('business:bill:notify')")
    @Log(title = "账单管理", businessType = BusinessType.OTHER)
    @PostMapping("/notify/{billId}")
    public AjaxResult notifyBill(@PathVariable Long billId,
            @RequestParam(required = false) String msgType)
    {
        int count = weChatMessageService.notifyBill(billId, msgType);
        return success().put("count", count);
    }

    /** 发票打印数据：返回账单 + 明细 + 收款记录，前端打开 HTML 打印 */
    @PreAuthorize("@ss.hasPermi('business:bill:invoice')")
    @GetMapping("/invoice/{billId}")
    public AjaxResult invoice(@PathVariable Long billId)
    {
        Bill bill = billService.selectBillById(billId);
        if (bill == null)
        {
            return error("账单不存在");
        }
        List<BillItem> items = billService.selectBillItemsByBillId(billId);
        List<PaymentRecord> records = billService.selectPaymentRecords(billId);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("bill", bill);
        data.put("items", items);
        data.put("records", records);
        return success(data);
    }

    /**
     * 后端生成发票 PDF 并返回文件流（iText + Windows 中文字体）
     * 演示用：部署到 Linux 需替换 InvoicePdfGenerator.FONT_PATH 为可用中文字体
     */
    @PreAuthorize("@ss.hasPermi('business:bill:invoice')")
    @GetMapping("/invoice/{billId}/pdf")
    public void invoicePdf(@PathVariable Long billId, HttpServletResponse response) throws IOException
    {
        Bill bill = billService.selectBillById(billId);
        if (bill == null)
        {
            throw new ServiceException("账单不存在");
        }
        List<BillItem> items = billService.selectBillItemsByBillId(billId);
        List<PaymentRecord> records = billService.selectPaymentRecords(billId);

        byte[] pdfBytes;
        try
        {
            pdfBytes = InvoicePdfGenerator.generate(bill, items, records);
        }
        catch (Exception e)
        {
            throw new ServiceException("PDF 生成失败：" + e.getMessage());
        }

        String fileName = "发票_" + bill.getBillNo() + ".pdf";
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");

        response.reset();
        response.setContentType("application/pdf");
        response.setContentLength(pdfBytes.length);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded);
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    /** 删除账单 */
    @PreAuthorize("@ss.hasPermi('business:bill:remove')")
    @Log(title = "账单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(billService.deleteBillByIds(ids));
    }

    /** 导出账单 */
    @PreAuthorize("@ss.hasPermi('business:bill:export')")
    @Log(title = "账单管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Bill bill)
    {
        List<Bill> list = billService.selectBillList(bill);
        ExcelUtil<Bill> util = new ExcelUtil<Bill>(Bill.class);
        util.exportExcel(response, list, "账单数据");
    }

    /** 获取当前机构ID（工具方法） */
    private Long getTenantId()
    {
        try
        {
            return com.ruoyi.common.utils.SecurityUtils.getTenantId();
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
