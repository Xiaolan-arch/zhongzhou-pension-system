package com.ruoyi.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.SysTenant;
import com.ruoyi.system.service.ISysTenantService;

/**
 * 机构(租户)管理
 */
@RestController
@RequestMapping("/system/tenant")
public class SysTenantController extends BaseController
{
    @Autowired
    private ISysTenantService tenantService;

    /**
     * 查询机构列表
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysTenant tenant)
    {
        startPage();
        List<SysTenant> list = tenantService.selectTenantList(tenant);
        return getDataTable(list);
    }

    /**
     * 获取机构详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:query')")
    @GetMapping(value = "/{tenantId}")
    public AjaxResult getInfo(@PathVariable Long tenantId)
    {
        return success(tenantService.selectTenantById(tenantId));
    }

    /**
     * 新增机构
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:add')")
    @Log(title = "机构管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysTenant tenant)
    {
        if (!tenantService.checkTenantNameUnique(tenant))
        {
            return error("新增机构'" + tenant.getTenantName() + "'失败，机构名称已存在");
        }
        return toAjax(tenantService.insertTenant(tenant));
    }

    /**
     * 修改机构
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:edit')")
    @Log(title = "机构管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysTenant tenant)
    {
        if (!tenantService.checkTenantNameUnique(tenant))
        {
            return error("修改机构'" + tenant.getTenantName() + "'失败，机构名称已存在");
        }
        return toAjax(tenantService.updateTenant(tenant));
    }

    /**
     * 删除机构
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:remove')")
    @Log(title = "机构管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tenantIds}")
    public AjaxResult remove(@PathVariable Long[] tenantIds)
    {
        return toAjax(tenantService.deleteTenantByIds(tenantIds));
    }
}
