package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.Bed;
import com.ruoyi.business.domain.vo.FloorVO;
import com.ruoyi.business.domain.vo.RoomDetailVO;

/**
 * 在住管理 服务层
 *
 * @author ruoyi
 */
public interface IResidenceService
{
    public List<FloorVO> selectOverview(Integer floorNo);

    public RoomDetailVO selectRoomDetail(Long roomId);

    public List<Bed> selectAvailableBeds();
}
