package com.ruoyi.web.controller.business;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.Deposit;
import com.ruoyi.business.service.IDepositService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 押金管理 控制器
 */
@RestController
@RequestMapping("/business/deposit")
public class DepositController extends BaseController
{
    @Autowired
    private IDepositService depositService;

    /** 查询押金记录列表 */
    @PreAuthorize("@ss.hasPermi('business:deposit:list')")
    @GetMapping("/list")
    public TableDataInfo list(Deposit deposit)
    {
        startPage();
        List<Deposit> list = depositService.selectDepositList(deposit);
        return getDataTable(list);
    }

    /** 查询老人押金可退余额 */
    @PreAuthorize("@ss.hasPermi('business:deposit:list')")
    @GetMapping("/balance/{elderId}")
    public AjaxResult balance(@PathVariable Long elderId)
    {
        BigDecimal balance = depositService.selectElderDepositBalance(elderId);
        return success().put("balance", balance);
    }

    /** 收押金 */
    @PreAuthorize("@ss.hasPermi('business:deposit:add')")
    @Log(title = "押金管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@RequestBody Deposit deposit)
    {
        return toAjax(depositService.collectDeposit(deposit));
    }

    /** 退押金 */
    @PreAuthorize("@ss.hasPermi('business:deposit:refund')")
    @Log(title = "押金管理", businessType = BusinessType.UPDATE)
    @PostMapping("/refund")
    public AjaxResult refund(@RequestBody Deposit deposit)
    {
        return toAjax(depositService.refundDeposit(deposit));
    }

    /** 导出押金记录 */
    @PreAuthorize("@ss.hasPermi('business:deposit:list')")
    @Log(title = "押金管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(javax.servlet.http.HttpServletResponse response, Deposit deposit)
    {
        List<Deposit> list = depositService.selectDepositList(deposit);
        ExcelUtil<Deposit> util = new ExcelUtil<Deposit>(Deposit.class);
        util.exportExcel(response, list, "押金记录");
    }
}
