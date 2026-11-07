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
import com.ruoyi.business.domain.ServiceOrder;
import com.ruoyi.business.service.IServiceOrderService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 服务订单
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/service/order")
public class ServiceOrderController extends BaseController
{
    @Autowired
    private IServiceOrderService serviceOrderService;

    /**
     * 查询服务订单列表
     */
    @PreAuthorize("@ss.hasPermi('business:service:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(ServiceOrder serviceOrder)
    {
        startPage();
        List<ServiceOrder> list = serviceOrderService.selectServiceOrderList(serviceOrder);
        return getDataTable(list);
    }

    /**
     * 获取服务订单详情
     */
    @PreAuthorize("@ss.hasPermi('business:service:order:list')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(serviceOrderService.selectServiceOrderById(id));
    }

    /**
     * 新增服务订单
     */
    @PreAuthorize("@ss.hasPermi('business:service:order:add')")
    @Log(title = "服务订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ServiceOrder serviceOrder)
    {
        serviceOrder.setCreateBy(getUsername());
        return toAjax(serviceOrderService.insertServiceOrder(serviceOrder));
    }

    /**
     * 修改服务订单
     */
    @PreAuthorize("@ss.hasPermi('business:service:order:edit')")
    @Log(title = "服务订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ServiceOrder serviceOrder)
    {
        serviceOrder.setUpdateBy(getUsername());
        return toAjax(serviceOrderService.updateServiceOrder(serviceOrder));
    }

    /**
     * 删除服务订单
     */
    @PreAuthorize("@ss.hasPermi('business:service:order:remove')")
    @Log(title = "服务订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(serviceOrderService.deleteServiceOrderByIds(ids));
    }

    /**
     * 执行服务订单
     */
    @PreAuthorize("@ss.hasPermi('business:service:order:execute')")
    @Log(title = "服务订单", businessType = BusinessType.UPDATE)
    @PutMapping("/execute/{id}")
    public AjaxResult execute(@PathVariable Long id)
    {
        return toAjax(serviceOrderService.executeOrder(id));
    }

    /**
     * 完成服务订单（可带 serviceRemark 服务记录备注）
     */
    @PreAuthorize("@ss.hasPermi('business:service:order:execute')")
    @Log(title = "服务订单", businessType = BusinessType.UPDATE)
    @PutMapping("/complete/{id}")
    public AjaxResult complete(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, String> body)
    {
        String remark = (body != null && body.containsKey("serviceRemark")) ? body.get("serviceRemark") : null;
        return toAjax(serviceOrderService.completeOrder(id, remark));
    }

    /**
     * 护理员"我的待办"：当前登录用户的待办订单
     * 管理员返回全部待执行，护理员返回指派给自己的待办
     */
    @GetMapping("/my")
    public AjaxResult my()
    {
        List<ServiceOrder> list = serviceOrderService.selectMyOrders();
        return success(list);
    }
}
