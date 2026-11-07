package com.ruoyi.web.controller.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.Checkout;
import com.ruoyi.business.service.ICheckoutService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 退住办理 控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/checkout")
public class CheckoutController extends BaseController
{
    @Autowired
    private ICheckoutService checkoutService;

    /**
     * 查询退住申请列表
     */
    @PreAuthorize("@ss.hasPermi('business:checkout:list')")
    @GetMapping("/list")
    public TableDataInfo list(Checkout checkout)
    {
        startPage();
        List<Checkout> list = checkoutService.selectCheckoutList(checkout);
        return getDataTable(list);
    }

    /**
     * 查询退住申请详情
     */
    @PreAuthorize("@ss.hasPermi('business:checkout:list')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(checkoutService.selectCheckoutById(id));
    }

    /**
     * 创建退住申请
     */
    @PreAuthorize("@ss.hasPermi('business:checkout:edit')")
    @Log(title = "退住办理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Checkout checkout)
    {
        return toAjax(checkoutService.insertCheckout(checkout));
    }

    /**
     * 保存当前步骤表单字段
     */
    @PreAuthorize("@ss.hasPermi('business:checkout:edit')")
    @Log(title = "退住办理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Checkout checkout)
    {
        return toAjax(checkoutService.updateCheckout(checkout));
    }

    /**
     * 提交退住向导步骤（1-6）
     */
    @PreAuthorize("@ss.hasPermi('business:checkout:edit')")
    @Log(title = "退住办理", businessType = BusinessType.UPDATE)
    @PutMapping("/step/{id}/{step}")
    public AjaxResult step(@PathVariable Long id, @PathVariable Integer step, @RequestBody Checkout checkout)
    {
        return toAjax(checkoutService.submitStep(id, step, checkout));
    }

    /**
     * 完成退住
     */
    @PreAuthorize("@ss.hasPermi('business:checkout:complete')")
    @Log(title = "退住办理", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/complete")
    public AjaxResult complete(@PathVariable Long id, @RequestBody Checkout checkout)
    {
        return toAjax(checkoutService.complete(id, checkout.getSettleAmount(), checkout.getRefundMethod()));
    }

    /**
     * 删除退住申请
     */
    @PreAuthorize("@ss.hasPermi('business:checkout:remove')")
    @Log(title = "退住办理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(checkoutService.deleteCheckoutByIds(ids));
    }
}
