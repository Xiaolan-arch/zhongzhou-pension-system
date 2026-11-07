package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.Room;

/**
 * 房间 服务层
 *
 * @author ruoyi
 */
public interface IRoomService
{
    public List<Room> selectRoomList(Room room);

    public Room selectRoomById(Long roomId);

    public int insertRoom(Room room);

    public int updateRoom(Room room);

    public int deleteRoomByIds(Long[] roomIds);
}
