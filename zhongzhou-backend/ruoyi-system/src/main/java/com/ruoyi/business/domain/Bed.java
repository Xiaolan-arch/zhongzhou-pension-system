package com.ruoyi.business.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 床位 biz_bed
 *
 * @author ruoyi
 */
public class Bed extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 床位ID */
    private Long bedId;

    /** 房间ID */
    private Long roomId;

    /** 床位号 */
    private String bedNo;

    /** 状态（0空闲 1已入住 2外出中） */
    private String status;

    /** 老人ID */
    private Long elderId;

    /** 机构ID */
    private Long tenantId;

    /** 删除标志 */
    private String delFlag;

    /** 楼层（展示字段，聚合查询用） */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer floorNo;

    /** 房间号（展示字段，聚合查询用） */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String roomNo;

    /** 房型（展示字段，聚合查询用） */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String roomType;

    /** 老人姓名（展示字段） */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String elderName;

    /** 性别（展示字段） */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String gender;

    /** 年龄（展示字段） */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer age;

    /** 护理等级（展示字段） */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String careLevel;

    /** 入住日期（展示字段） */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date checkinDate;

    /** 床位护理员（展示字段） */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Nurse> nurses;

    public Long getBedId()
    {
        return bedId;
    }

    public void setBedId(Long bedId)
    {
        this.bedId = bedId;
    }

    public Long getRoomId()
    {
        return roomId;
    }

    public void setRoomId(Long roomId)
    {
        this.roomId = roomId;
    }

    public String getBedNo()
    {
        return bedNo;
    }

    public void setBedNo(String bedNo)
    {
        this.bedNo = bedNo;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getElderId()
    {
        return elderId;
    }

    public void setElderId(Long elderId)
    {
        this.elderId = elderId;
    }

    public Long getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(Long tenantId)
    {
        this.tenantId = tenantId;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
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

    public String getElderName()
    {
        return elderName;
    }

    public void setElderName(String elderName)
    {
        this.elderName = elderName;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public Integer getAge()
    {
        return age;
    }

    public void setAge(Integer age)
    {
        this.age = age;
    }

    public String getCareLevel()
    {
        return careLevel;
    }

    public void setCareLevel(String careLevel)
    {
        this.careLevel = careLevel;
    }

    public Date getCheckinDate()
    {
        return checkinDate;
    }

    public void setCheckinDate(Date checkinDate)
    {
        this.checkinDate = checkinDate;
    }

    public List<Nurse> getNurses()
    {
        return nurses;
    }

    public void setNurses(List<Nurse> nurses)
    {
        this.nurses = nurses;
    }
}
