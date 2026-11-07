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
import com.ruoyi.business.domain.Medication;
import com.ruoyi.business.service.IMedicationService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 用药提醒管理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/medication")
public class MedicationController extends BaseController
{
    @Autowired
    private IMedicationService medicationService;

    @PreAuthorize("@ss.hasPermi('business:medication:list')")
    @GetMapping("/list")
    public TableDataInfo list(Medication medication)
    {
        startPage();
        List<Medication> list = medicationService.selectMedicationList(medication);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:medication:list')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(medicationService.selectMedicationById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:medication:add')")
    @Log(title = "用药管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Medication medication)
    {
        return toAjax(medicationService.insertMedication(medication));
    }

    @PreAuthorize("@ss.hasPermi('business:medication:edit')")
    @Log(title = "用药管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Medication medication)
    {
        return toAjax(medicationService.updateMedication(medication));
    }

    @PreAuthorize("@ss.hasPermi('business:medication:remove')")
    @Log(title = "用药管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(medicationService.deleteMedicationByIds(ids));
    }
}
