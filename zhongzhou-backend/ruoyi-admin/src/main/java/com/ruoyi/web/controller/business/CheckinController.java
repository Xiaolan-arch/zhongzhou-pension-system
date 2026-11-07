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
import com.ruoyi.business.domain.Checkin;
import com.ruoyi.business.service.ICheckinService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 入住办理 控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/checkin")
public class CheckinController extends BaseController
{
    @Autowired
    private ICheckinService checkinService;

    /**
     * 查询入住申请列表
     */
    @PreAuthorize("@ss.hasPermi('business:checkin:list')")
    @GetMapping("/list")
    public TableDataInfo list(Checkin checkin)
    {
        startPage();
        List<Checkin> list = checkinService.selectCheckinList(checkin);
        return getDataTable(list);
    }

    /**
     * 查询入住申请详情
     */
    @PreAuthorize("@ss.hasPermi('business:checkin:list')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(checkinService.selectCheckinById(id));
    }

    /**
     * 创建入住申请草稿
     */
    @PreAuthorize("@ss.hasPermi('business:checkin:edit')")
    @Log(title = "入住办理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Checkin checkin)
    {
        return toAjax(checkinService.insertCheckin(checkin));
    }

    /**
     * 保存当前步骤表单字段
     */
    @PreAuthorize("@ss.hasPermi('business:checkin:edit')")
    @Log(title = "入住办理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Checkin checkin)
    {
        return toAjax(checkinService.updateCheckin(checkin));
    }

    /**
     * 提交入住向导步骤（1-6）
     */
    @PreAuthorize("@ss.hasPermi('business:checkin:edit')")
    @Log(title = "入住办理", businessType = BusinessType.UPDATE)
    @PutMapping("/step/{id}/{step}")
    public AjaxResult step(@PathVariable Long id, @PathVariable Integer step, @RequestBody Checkin checkin)
    {
        return toAjax(checkinService.submitStep(id, step, checkin));
    }

    /**
     * 入住审核
     */
    @PreAuthorize("@ss.hasPermi('business:checkin:audit')")
    @Log(title = "入住审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public AjaxResult audit(@RequestBody Checkin checkin)
    {
        return toAjax(checkinService.audit(checkin));
    }

    /**
     * 完成入住
     */
    @PreAuthorize("@ss.hasPermi('business:checkin:complete')")
    @Log(title = "入住办理", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/complete")
    public AjaxResult complete(@PathVariable Long id)
    {
        return toAjax(checkinService.complete(id));
    }

    /**
     * 驳回后重新提交（9 已驳回 → 2 待审核）
     */
    @PreAuthorize("@ss.hasPermi('business:checkin:edit')")
    @Log(title = "入住办理", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/resubmit")
    public AjaxResult resubmit(@PathVariable Long id)
    {
        return toAjax(checkinService.resubmit(id));
    }

    /**
     * 删除入住申请
     */
    @PreAuthorize("@ss.hasPermi('business:checkin:remove')")
    @Log(title = "入住办理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(checkinService.deleteCheckinByIds(ids));
    }
}
