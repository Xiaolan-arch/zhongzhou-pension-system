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
import com.ruoyi.business.domain.Bed;
import com.ruoyi.business.mapper.BedMapper;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 床位管理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/bed")
public class BedController extends BaseController
{
    @Autowired
    private BedMapper bedMapper;

    /**
     * 查询床位列表
     */
    @PreAuthorize("@ss.hasPermi('business:bed:list')")
    @GetMapping("/list")
    public TableDataInfo list(Bed bed)
    {
        startPage();
        List<Bed> list = bedMapper.selectBedList(bed);
        return getDataTable(list);
    }

    /**
     * 获取床位详情
     */
    @PreAuthorize("@ss.hasPermi('business:bed:list')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(bedMapper.selectBedById(id));
    }

    /**
     * 新增床位
     */
    @PreAuthorize("@ss.hasPermi('business:bed:add')")
    @Log(title = "床位管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Bed bed)
    {
        if (StringUtils.isNull(bed.getTenantId()))
        {
            bed.setTenantId(SecurityUtils.getTenantId());
        }
        bed.setCreateBy(getUsername());
        return toAjax(bedMapper.insertBed(bed));
    }

    /**
     * 修改床位
     */
    @PreAuthorize("@ss.hasPermi('business:bed:edit')")
    @Log(title = "床位管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Bed bed)
    {
        bed.setUpdateBy(getUsername());
        return toAjax(bedMapper.updateBed(bed));
    }

    /**
     * 删除床位
     */
    @PreAuthorize("@ss.hasPermi('business:bed:remove')")
    @Log(title = "床位管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bedMapper.deleteBedByIds(ids));
    }

    /**
     * 查询可分配床位（状态为空闲）
     */
    @PreAuthorize("@ss.hasPermi('business:bed:list')")
    @GetMapping("/available")
    public AjaxResult available()
    {
        return success(bedMapper.selectAvailableBeds());
    }

    /**
     * 查询指定房间下的床位
     */
    @PreAuthorize("@ss.hasPermi('business:bed:list')")
    @GetMapping("/room/{roomId}")
    public AjaxResult listByRoom(@PathVariable Long roomId)
    {
        Bed bed = new Bed();
        bed.setRoomId(roomId);
        return success(bedMapper.selectBedList(bed));
    }
}
