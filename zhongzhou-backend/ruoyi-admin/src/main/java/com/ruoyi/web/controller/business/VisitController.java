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
import com.ruoyi.business.domain.Visit;
import com.ruoyi.business.service.IVisitService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 来访预约
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/visit")
public class VisitController extends BaseController
{
    @Autowired
    private IVisitService visitService;

    /**
     * 查询来访列表
     */
    @PreAuthorize("@ss.hasPermi('business:visit:list')")
    @GetMapping("/list")
    public TableDataInfo list(Visit visit)
    {
        startPage();
        List<Visit> list = visitService.selectVisitList(visit);
        return getDataTable(list);
    }

    /**
     * 获取来访详情
     */
    @PreAuthorize("@ss.hasPermi('business:visit:list')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(visitService.selectVisitById(id));
    }

    /**
     * 新增来访
     */
    @PreAuthorize("@ss.hasPermi('business:visit:add')")
    @Log(title = "来访管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Visit visit)
    {
        visit.setCreateBy(getUsername());
        return toAjax(visitService.insertVisit(visit));
    }

    /**
     * 修改来访
     */
    @PreAuthorize("@ss.hasPermi('business:visit:edit')")
    @Log(title = "来访管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Visit visit)
    {
        visit.setUpdateBy(getUsername());
        return toAjax(visitService.updateVisit(visit));
    }

    /**
     * 删除来访
     */
    @PreAuthorize("@ss.hasPermi('business:visit:remove')")
    @Log(title = "来访管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(visitService.deleteVisitByIds(ids));
    }

    /**
     * 确认来访
     */
    @PreAuthorize("@ss.hasPermi('business:visit:confirm')")
    @Log(title = "来访管理", businessType = BusinessType.UPDATE)
    @PutMapping("/confirm/{id}")
    public AjaxResult confirm(@PathVariable Long id)
    {
        return toAjax(visitService.confirmVisit(id));
    }

    /**
     * 取消来访
     */
    @PreAuthorize("@ss.hasPermi('business:visit:confirm')")
    @Log(title = "来访管理", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{id}")
    public AjaxResult cancel(@PathVariable Long id)
    {
        return toAjax(visitService.cancelVisit(id));
    }

    /**
     * 到访核销
     */
    @PreAuthorize("@ss.hasPermi('business:visit:checkin')")
    @Log(title = "来访核销", businessType = BusinessType.UPDATE)
    @PutMapping("/checkin/{id}")
    public AjaxResult checkin(@PathVariable Long id)
    {
        return toAjax(visitService.checkinVisit(id));
    }
}
