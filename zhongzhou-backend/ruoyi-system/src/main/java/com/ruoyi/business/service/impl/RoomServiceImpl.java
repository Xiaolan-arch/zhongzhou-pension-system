package com.ruoyi.business.service.impl;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.Bed;
import com.ruoyi.business.domain.Room;
import com.ruoyi.business.mapper.BedMapper;
import com.ruoyi.business.mapper.RoomMapper;
import com.ruoyi.business.service.IRoomService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 房间 服务层处理
 *
 * @author ruoyi
 */
@Service
public class RoomServiceImpl implements IRoomService
{
    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private BedMapper bedMapper;

    @Override
    public List<Room> selectRoomList(Room room)
    {
        return roomMapper.selectRoomList(room);
    }

    @Override
    public Room selectRoomById(Long roomId)
    {
        return roomMapper.selectRoomById(roomId);
    }

    /**
     * 新增房间，并按床位数自动生成床位
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRoom(Room room)
    {
        if (StringUtils.isNull(room.getTenantId()))
        {
            room.setTenantId(SecurityUtils.getTenantId());
        }
        if (StringUtils.isEmpty(room.getCreateBy()))
        {
            room.setCreateBy(SecurityUtils.getUsername());
        }
        room.setCreateTime(DateUtils.getNowDate());
        int rows = roomMapper.insertRoom(room);

        int bedCount = room.getBedCount() == null ? 0 : room.getBedCount();
        for (int i = 1; i <= bedCount; i++)
        {
            Bed bed = new Bed();
            bed.setRoomId(room.getRoomId());
            bed.setBedNo(room.getRoomNo() + i);
            bed.setStatus("0");
            bed.setTenantId(room.getTenantId());
            bed.setCreateBy(room.getCreateBy());
            bed.setCreateTime(room.getCreateTime());
            bedMapper.insertBed(bed);
        }
        return rows;
    }

    @Override
    public int updateRoom(Room room)
    {
        room.setUpdateBy(SecurityUtils.getUsername());
        room.setUpdateTime(DateUtils.getNowDate());
        return roomMapper.updateRoom(room);
    }

    /**
     * 逻辑删除房间及其床位，存在已入住床位时不允许删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRoomByIds(Long[] roomIds)
    {
        for (Long roomId : roomIds)
        {
            Room room = roomMapper.selectRoomById(roomId);
            if (room == null)
            {
                continue;
            }
            List<Bed> beds = bedMapper.selectBedListByRoomIds(Collections.singletonList(roomId));
            for (Bed bed : beds)
            {
                if ("1".equals(bed.getStatus()))
                {
                    throw new ServiceException("房间" + room.getRoomNo() + "存在已入住床位，不能删除");
                }
            }
        }
        int rows = roomMapper.deleteRoomByIds(roomIds);
        for (Long roomId : roomIds)
        {
            bedMapper.deleteBedByRoomId(roomId);
        }
        return rows;
    }
}
