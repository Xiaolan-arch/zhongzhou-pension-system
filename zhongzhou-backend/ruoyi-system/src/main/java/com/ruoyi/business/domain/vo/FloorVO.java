package com.ruoyi.business.domain.vo;

import java.util.List;

/**
 * 楼层聚合视图
 *
 * @author ruoyi
 */
public class FloorVO
{
    /** 楼层 */
    private Integer floorNo;

    /** 房间列表 */
    private List<RoomVO> rooms;

    public FloorVO(Integer floorNo, List<RoomVO> rooms)
    {
        this.floorNo = floorNo;
        this.rooms = rooms;
    }

    public Integer getFloorNo()
    {
        return floorNo;
    }

    public void setFloorNo(Integer floorNo)
    {
        this.floorNo = floorNo;
    }

    public List<RoomVO> getRooms()
    {
        return rooms;
    }

    public void setRooms(List<RoomVO> rooms)
    {
        this.rooms = rooms;
    }
}
