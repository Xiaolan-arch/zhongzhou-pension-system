package com.ruoyi.business.domain.vo;

/**
 * 老人当前床位位置
 *
 * @author ruoyi
 */
public class BedLocation
{
    /** 楼层 */
    private String floorNo;

    /** 房间号 */
    private String roomNo;

    /** 床位号 */
    private String bedNo;

    public String getFloorNo()
    {
        return floorNo;
    }

    public void setFloorNo(String floorNo)
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

    public String getBedNo()
    {
        return bedNo;
    }

    public void setBedNo(String bedNo)
    {
        this.bedNo = bedNo;
    }
}
