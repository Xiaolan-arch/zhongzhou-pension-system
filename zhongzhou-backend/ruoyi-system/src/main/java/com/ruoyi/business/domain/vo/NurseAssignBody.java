package com.ruoyi.business.domain.vo;

import java.util.List;

/**
 * 护理员分配请求体
 *
 * @author ruoyi
 */
public class NurseAssignBody
{
    /** 床位ID（单床分配） */
    private Long bedId;

    /** 楼层（整层批量分配） */
    private Integer floorNo;

    /** 护理员ID列表 */
    private List<Long> nurseIds;

    public Long getBedId()
    {
        return bedId;
    }

    public void setBedId(Long bedId)
    {
        this.bedId = bedId;
    }

    public Integer getFloorNo()
    {
        return floorNo;
    }

    public void setFloorNo(Integer floorNo)
    {
        this.floorNo = floorNo;
    }

    public List<Long> getNurseIds()
    {
        return nurseIds;
    }

    public void setNurseIds(List<Long> nurseIds)
    {
        this.nurseIds = nurseIds;
    }
}
