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
import com.ruoyi.business.domain.CareLog;
import com.ruoyi.business.service.ICareLogService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 护理日志管理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/careLog")
public class CareLogController extends BaseController
{
    @Autowired
    private ICareLogService careLogService;

    @PreAuthorize("@ss.hasPermi('business:careLog:list')")
    @GetMapping("/list")
    public TableDataInfo list(CareLog careLog)
    {
        startPage();
        List<CareLog> list = careLogService.selectCareLogList(careLog);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:careLog:list')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(careLogService.selectCareLogById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:careLog:add')")
    @Log(title = "护理日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CareLog careLog)
    {
        return toAjax(careLogService.insertCareLog(careLog));
    }

    @PreAuthorize("@ss.hasPermi('business:careLog:edit')")
    @Log(title = "护理日志", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CareLog careLog)
    {
        return toAjax(careLogService.updateCareLog(careLog));
    }

    @PreAuthorize("@ss.hasPermi('business:careLog:remove')")
    @Log(title = "护理日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(careLogService.deleteCareLogByIds(ids));
    }
}
