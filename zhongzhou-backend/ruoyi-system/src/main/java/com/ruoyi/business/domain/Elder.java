package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 老人档案 biz_elder
 *
 * @author ruoyi
 */
public class Elder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 老人ID */
    private Long elderId;

    /** 老人姓名 */
    private String elderName;

    /** 性别（0男 1女） */
    private String gender;

    /** 身份证号 */
    private String idCard;

    /** 出生日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /** 年龄 */
    private Integer age;

    /** 联系电话 */
    private String phone;

    /** 户籍地址 */
    private String address;

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

    /** 家属电话 */
    private String familyPhone;

    /** 机构ID */
    private Long tenantId;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

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

    public String getIdCard()
    {
        return idCard;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }

    public Date getBirthday()
    {
        return birthday;
    }

    public void setBirthday(Date birthday)
    {
        this.birthday = birthday;
    }

    public Integer getAge()
    {
        return age;
    }

    public void setAge(Integer age)
    {
        this.age = age;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
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

    public String getFamilyPhone()
    {
        return familyPhone;
    }

    public void setFamilyPhone(String familyPhone)
    {
        this.familyPhone = familyPhone;
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
}
