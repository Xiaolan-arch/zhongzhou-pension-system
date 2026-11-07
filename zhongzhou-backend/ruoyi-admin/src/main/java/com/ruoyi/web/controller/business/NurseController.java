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
import com.ruoyi.business.domain.Nurse;
import com.ruoyi.business.domain.vo.NurseAssignBody;
import com.ruoyi.business.service.INurseService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 护理员与床位护理员分配
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/nurse")
public class NurseController extends BaseController
{
    @Autowired
    private INurseService nurseService;

    /**
     * 查询护理员列表
     */
    @PreAuthorize("@ss.hasPermi('business:nurse:list') or @ss.hasPermi('business:service:task')")
    @GetMapping("/list")
    public TableDataInfo list(Nurse nurse)
    {
        startPage();
        List<Nurse> list = nurseService.selectNurseList(nurse);
        return getDataTable(list);
    }

    /**
     * 单床护理员分配
     */
    @PreAuthorize("@ss.hasPermi('business:service:assign')")
    @Log(title = "护理任务", businessType = BusinessType.UPDATE)
    @PutMapping("/assign")
    public AjaxResult assign(@RequestBody NurseAssignBody body)
    {
        nurseService.assign(body.getBedId(), body.getNurseIds());
        return success();
    }

    /**
     * 整层批量分配护理员
     */
    @PreAuthorize("@ss.hasPermi('business:service:assign')")
    @Log(title = "护理任务", businessType = BusinessType.UPDATE)
    @PutMapping("/batchAssign")
    public AjaxResult batchAssign(@RequestBody NurseAssignBody body)
    {
        nurseService.batchAssign(body.getFloorNo(), body.getNurseIds());
        return success();
    }

    /**
     * 查询床位护理员
     */
    @PreAuthorize("@ss.hasPermi('business:service:task')")
    @GetMapping("/bed/{bedId}")
    public AjaxResult bedNurses(@PathVariable Long bedId)
    {
        return success(nurseService.selectNursesByBedId(bedId));
    }

    /**
     * 获取护理员详情
     */
    @PreAuthorize("@ss.hasPermi('business:nurse:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(nurseService.selectNurseById(id));
    }

    /**
     * 新增护理员
     */
    @PreAuthorize("@ss.hasPermi('business:nurse:add')")
    @Log(title = "护理员管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Nurse nurse)
    {
        return toAjax(nurseService.insertNurse(nurse));
    }

    /**
     * 修改护理员
     */
    @PreAuthorize("@ss.hasPermi('business:nurse:edit')")
    @Log(title = "护理员管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Nurse nurse)
    {
        return toAjax(nurseService.updateNurse(nurse));
    }

    /**
     * 删除护理员
     */
    @PreAuthorize("@ss.hasPermi('business:nurse:remove')")
    @Log(title = "护理员管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(nurseService.deleteNurseByIds(ids));
    }
}
