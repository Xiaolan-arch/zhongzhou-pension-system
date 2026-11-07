package com.ruoyi.web.controller.business;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.business.domain.HealthRecord;
import com.ruoyi.business.service.IHealthRecordService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 老人体征记录 Controller
 */
@RestController
@RequestMapping("/business/health-record")
public class HealthRecordController extends BaseController
{
    @Autowired
    private IHealthRecordService healthRecordService;

    @PreAuthorize("@ss.hasPermi('business:health:list')")
    @GetMapping("/list")
    public TableDataInfo list(HealthRecord healthRecord)
    {
        startPage();
        List<HealthRecord> list = healthRecordService.selectHealthRecordList(healthRecord);
        return getDataTable(list);
    }

    /** 按老人查最近 N 条体征（AI 报告/老人详情用） */
    @GetMapping("/recent")
    public AjaxResult recent(@RequestParam Long elderId,
                             @RequestParam(defaultValue = "30") int limit)
    {
        if (limit > 90) limit = 90;
        List<HealthRecord> list = healthRecordService.selectRecentByElderId(elderId, limit);
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('business:health:export')")
    @Log(title = "体征记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HealthRecord healthRecord)
    {
        List<HealthRecord> list = healthRecordService.selectHealthRecordList(healthRecord);
        ExcelUtil<HealthRecord> util = new ExcelUtil<>(HealthRecord.class);
        util.exportExcel(response, list, "老人体征记录");
    }

    @PreAuthorize("@ss.hasPermi('business:health:query')")
    @GetMapping("/{recordId}")
    public AjaxResult getInfo(@PathVariable Long recordId)
    {
        return success(healthRecordService.selectHealthRecordById(recordId));
    }

    @PreAuthorize("@ss.hasPermi('business:health:add')")
    @Log(title = "体征记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HealthRecord healthRecord)
    {
        return toAjax(healthRecordService.insertHealthRecord(healthRecord));
    }

    @PreAuthorize("@ss.hasPermi('business:health:edit')")
    @Log(title = "体征记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HealthRecord healthRecord)
    {
        return toAjax(healthRecordService.updateHealthRecord(healthRecord));
    }

    @PreAuthorize("@ss.hasPermi('business:health:remove')")
    @Log(title = "体征记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(healthRecordService.deleteHealthRecordByIds(recordIds));
    }
}
