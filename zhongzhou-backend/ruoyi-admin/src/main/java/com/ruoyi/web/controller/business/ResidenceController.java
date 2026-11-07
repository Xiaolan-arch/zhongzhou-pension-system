package com.ruoyi.web.controller.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.service.IResidenceService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 在住管理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/residence")
public class ResidenceController extends BaseController
{
    @Autowired
    private IResidenceService residenceService;

    /**
     * 楼层/房间/床位/老人聚合视图
     */
    @PreAuthorize("@ss.hasPermi('business:residence:list')")
    @GetMapping("/overview")
    public AjaxResult overview(@RequestParam(value = "floorNo", required = false) Integer floorNo)
    {
        return success(residenceService.selectOverview(floorNo));
    }

    /**
     * 房间详情（含床位老人列表与入住率）
     */
    @PreAuthorize("@ss.hasPermi('business:residence:list')")
    @GetMapping("/room/{roomId}")
    public AjaxResult roomDetail(@PathVariable Long roomId)
    {
        return success(residenceService.selectRoomDetail(roomId));
    }

    /**
     * 空闲床位列表
     */
    @PreAuthorize("@ss.hasPermi('business:residence:list')")
    @GetMapping("/available-beds")
    public AjaxResult availableBeds()
    {
        return success(residenceService.selectAvailableBeds());
    }
}
