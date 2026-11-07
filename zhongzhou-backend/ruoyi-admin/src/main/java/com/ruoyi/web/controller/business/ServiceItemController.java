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
import com.ruoyi.business.domain.ServiceItem;
import com.ruoyi.business.service.IServiceItemService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 服务项目
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/service/item")
public class ServiceItemController extends BaseController
{
    @Autowired
    private IServiceItemService serviceItemService;

    /**
     * 查询服务项目列表
     */
    @PreAuthorize("@ss.hasPermi('business:service:item:list')")
    @GetMapping("/list")
    public TableDataInfo list(ServiceItem serviceItem)
    {
        startPage();
        List<ServiceItem> list = serviceItemService.selectServiceItemList(serviceItem);
        return getDataTable(list);
    }

    /**
     * 获取服务项目详情
     */
    @PreAuthorize("@ss.hasPermi('business:service:item:list')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(serviceItemService.selectServiceItemById(id));
    }

    /**
     * 新增服务项目
     */
    @PreAuthorize("@ss.hasPermi('business:service:item:add')")
    @Log(title = "服务项目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ServiceItem serviceItem)
    {
        serviceItem.setCreateBy(getUsername());
        return toAjax(serviceItemService.insertServiceItem(serviceItem));
    }

    /**
     * 修改服务项目
     */
    @PreAuthorize("@ss.hasPermi('business:service:item:edit')")
    @Log(title = "服务项目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ServiceItem serviceItem)
    {
        serviceItem.setUpdateBy(getUsername());
        return toAjax(serviceItemService.updateServiceItem(serviceItem));
    }

    /**
     * 删除服务项目
     */
    @PreAuthorize("@ss.hasPermi('business:service:item:remove')")
    @Log(title = "服务项目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(serviceItemService.deleteServiceItemByIds(ids));
    }
}
