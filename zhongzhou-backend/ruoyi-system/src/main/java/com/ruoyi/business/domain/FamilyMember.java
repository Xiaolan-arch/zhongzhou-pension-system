package com.ruoyi.business.domain;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 家属绑定关系 biz_family
 *
 * @author ruoyi
 */
public class FamilyMember implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 家属ID */
    private Long familyId;

    /** 家属姓名 */
    private String familyName;

    /** 手机号 */
    private String phone;

    /** 微信 openid */
    private String openid;

    /** 与老人关系 */
    private String relation;

    /** 绑定老人ID */
    private Long elderId;

    /** 状态（0正常 1停用） */
    private String status;

    /** 机构ID */
    private Long tenantId;

    /** 备注 */
    private String remark;

    /** 老人姓名（展示用） */
    private String elderName;

    /** 楼层（展示用） */
    private Integer floorNo;

    /** 房间号（展示用） */
    private String roomNo;

    /** 床位号（展示用） */
    private String bedNo;

    /** 绑定时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getFamilyId()
    {
        return familyId;
    }

    public void setFamilyId(Long familyId)
    {
        this.familyId = familyId;
    }

    public String getFamilyName()
    {
        return familyName;
    }

    public void setFamilyName(String familyName)
    {
        this.familyName = familyName;
    }

    public String getOpenid()
    {
        return openid;
    }

    public void setOpenid(String openid)
    {
        this.openid = openid;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getRelation()
    {
        return relation;
    }

    public void setRelation(String relation)
    {
        this.relation = relation;
    }

    public Long getElderId()
    {
        return elderId;
    }

    public void setElderId(Long elderId)
    {
        this.elderId = elderId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(Long tenantId)
    {
        this.tenantId = tenantId;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public String getElderName()
    {
        return elderName;
    }

    public void setElderName(String elderName)
    {
        this.elderName = elderName;
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

    public String getBedNo()
    {
        return bedNo;
    }

    public void setBedNo(String bedNo)
    {
        this.bedNo = bedNo;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
