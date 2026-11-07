package com.ruoyi.business.domain.vo;

import java.util.List;
import com.ruoyi.business.domain.Bed;

/**
 * 房间聚合视图
 *
 * @author ruoyi
 */
public class RoomVO
{
    /** 房间ID */
    private Long roomId;

    /** 房间号 */
    private String roomNo;

    /** 房型 */
    private String roomType;

    /** 床位数 */
    private Integer bedCount;

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

    public List<Bed> getBeds()
    {
        return beds;
    }

    public void setBeds(List<Bed> beds)
    {
        this.beds = beds;
    }
}
