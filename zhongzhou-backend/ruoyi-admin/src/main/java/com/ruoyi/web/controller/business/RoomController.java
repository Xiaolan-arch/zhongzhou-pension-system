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
import com.ruoyi.business.domain.Room;
import com.ruoyi.business.service.IRoomService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 房间管理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/room")
public class RoomController extends BaseController
{
    @Autowired
    private IRoomService roomService;

    /**
     * 查询房间列表
     */
    @PreAuthorize("@ss.hasPermi('business:room:list')")
    @GetMapping("/list")
    public TableDataInfo list(Room room)
    {
        startPage();
        List<Room> list = roomService.selectRoomList(room);
        return getDataTable(list);
    }

    /**
     * 获取房间详情
     */
    @PreAuthorize("@ss.hasPermi('business:room:list')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(roomService.selectRoomById(id));
    }

    /**
     * 新增房间，同时按床位数生成床位
     */
    @PreAuthorize("@ss.hasPermi('business:room:add')")
    @Log(title = "房间管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Room room)
    {
        room.setCreateBy(getUsername());
        return toAjax(roomService.insertRoom(room));
    }

    /**
     * 修改房间
     */
    @PreAuthorize("@ss.hasPermi('business:room:edit')")
    @Log(title = "房间管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Room room)
    {
        room.setUpdateBy(getUsername());
        return toAjax(roomService.updateRoom(room));
    }

    /**
     * 删除房间
     */
    @PreAuthorize("@ss.hasPermi('business:room:remove')")
    @Log(title = "房间管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(roomService.deleteRoomByIds(ids));
    }
}
