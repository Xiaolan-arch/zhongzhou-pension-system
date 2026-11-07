package com.ruoyi.business.domain.vo;

import java.util.List;
import com.ruoyi.business.domain.Bed;

/**
 * 房间详情视图
 *
 * @author ruoyi
 */
public class RoomDetailVO
{
    /** 房间ID */
    private Long roomId;

    /** 楼层 */
    private Integer floorNo;

    /** 房间号 */
    private String roomNo;

    /** 房型 */
    private String roomType;

    /** 床位数 */
    private Integer bedCount;

    /** 状态 */
    private String status;

    /** 已入住床位数 */
    private Integer occupiedBeds;

    /** 入住率（百分比，保留两位小数） */
    private Double occupancyRate;

    /** 床位列表 */
    private List<Bed> beds;

    public Long getRoomId()
    {
        return roomId;
    }

    public void setRoomId(Long roomId)
    {
        this.roomId = roomId;
    }

    public Integer getFloorNo()
    {
        return floorNo;
    }

    public void setFloorNo(Integer floorNo)
    {
        this.floorNo = floorNo;
    }

    public String getRoomNo()
    {
        return roomNo;
    }

    public void setRoomNo(String roomNo)
    {
        this.roomNo = roomNo;
    }

    public String getRoomType()
    {
        return roomType;
    }

    public void setRoomType(String roomType)
    {
        this.roomType = roomType;
    }

    public Integer getBedCount()
    {
        return bedCount;
    }

    public void setBedCount(Integer bedCount)
    {
        this.bedCount = bedCount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getOccupiedBeds()
    {
        return occupiedBeds;
    }

    public void setOccupiedBeds(Integer occupiedBeds)
    {
        this.occupiedBeds = occupiedBeds;
    }

    public Double getOccupancyRate()
    {
        return occupancyRate;
    }

    public void setOccupancyRate(Double occupancyRate)
    {
        this.occupancyRate = occupancyRate;
    }

    public List<Bed> getBeds()
    {
        return beds;
    }

    public void setBeds(List<Bed> beds)
    {
        this.beds = beds;
    }
}
