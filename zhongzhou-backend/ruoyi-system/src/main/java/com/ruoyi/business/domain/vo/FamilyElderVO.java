package com.ruoyi.business.domain.vo;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 家属端老人档案视图（老人信息 + 在住房间床位 + 护理员）
 *
 * @author ruoyi
 */
public class FamilyElderVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 老人ID */
    private Long elderId;

    /** 老人姓名 */
    private String elderName;

    /** 性别（0男 1女） */
    private String gender;

    /** 年龄 */
    private Integer age;

    /** 出生日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /** 身份证号 */
    private String idCard;

    /** 户籍地址 */
    private String address;

    /** 联系电话 */
    private String phone;

    /** 护理等级 */
    private String careLevel;

    /** 入住日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date checkinDate;

    /** 状态（0在住 1已退住 2待入住） */
    private String status;

    /** 头像 */
    private String avatar;

    /** 家属姓名 */
    private String familyName;

    /** 与老人关系 */
    private String familyRelation;

    /** 楼层 */
    private Integer floorNo;

    /** 房间号 */
    private String roomNo;

    /** 床位号 */
    private String bedNo;

    /** 护理员姓名（顿号分隔） */
    private String nurseNames;

    public Long getElderId()
    {
        return elderId;
    }

    public void setElderId(Long elderId)
    {
        this.elderId = elderId;
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

    public Date getBirthday()
    {
        return birthday;
    }

    public void setBirthday(Date birthday)
    {
        this.birthday = birthday;
    }

    public String getIdCard()
    {
        return idCard;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
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

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public String getFamilyName()
    {
        return familyName;
    }

    public void setFamilyName(String familyName)
    {
        this.familyName = familyName;
    }

    public String getFamilyRelation()
    {
        return familyRelation;
    }

    public void setFamilyRelation(String familyRelation)
    {
        this.familyRelation = familyRelation;
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

    public String getNurseNames()
    {
        return nurseNames;
    }

    public void setNurseNames(String nurseNames)
    {
        this.nurseNames = nurseNames;
    }
}
