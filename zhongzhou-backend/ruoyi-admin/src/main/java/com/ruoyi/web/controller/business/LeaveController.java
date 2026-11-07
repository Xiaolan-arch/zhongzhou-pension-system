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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.Leave;
import com.ruoyi.business.service.ILeaveService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 请假外出管理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/leave")
public class LeaveController extends BaseController
{
    @Autowired
    private ILeaveService leaveService;

    @PreAuthorize("@ss.hasPermi('business:leave:list')")
    @GetMapping("/list")
    public TableDataInfo list(Leave leave)
    {
        startPage();
        List<Leave> list = leaveService.selectLeaveList(leave);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:leave:list')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(leaveService.selectLeaveById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:leave:list')")
    @Log(title = "请假管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Leave leave)
    {
        return toAjax(leaveService.insertLeave(leave));
    }

    @PreAuthorize("@ss.hasPermi('business:leave:audit')")
    @Log(title = "请假管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Leave leave)
    {
        return toAjax(leaveService.updateLeave(leave));
    }

    @PreAuthorize("@ss.hasPermi('business:leave:remove')")
    @Log(title = "请假管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(leaveService.deleteLeaveByIds(ids));
    }

    /**
     * 审批（status=1 通过，status=2 驳回）
     */
    @PreAuthorize("@ss.hasPermi('business:leave:audit')")
    @Log(title = "请假审批", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/{id}")
    public AjaxResult audit(@PathVariable Long id, @RequestParam String status,
            @RequestParam(required = false) String auditOpinion)
    {
        return toAjax(leaveService.auditLeave(id, status, auditOpinion));
    }

    /**
     * 销假
     */
    @PreAuthorize("@ss.hasPermi('business:leave:audit')")
    @Log(title = "请假销假", businessType = BusinessType.UPDATE)
    @PutMapping("/back/{id}")
    public AjaxResult back(@PathVariable Long id)
    {
        return toAjax(leaveService.confirmBack(id));
    }
}
