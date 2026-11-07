package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 来访预约 biz_visit
 *
 * @author ruoyi
 */
public class Visit extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 来访ID */
    private Long visitId;

    /** 类型（0参观 1探访） */
    private String visitType;

    /** 来访人姓名 */
    private String visitorName;

    /** 来访人电话 */
    private String visitorPhone;

    /** 探访老人ID */
    private Long elderId;

    /** 探访老人姓名 */
    private String elderName;

    /** 来访日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date visitDate;

    /** 来访时间 */
    private String visitTime;

    /** 来访人数 */
    private Integer peopleCount;

    /** 状态（0待确认 1已确认 2已完成 3已取消） */
    private String status;

    /** 探访预约码 */
    private String visitCode;

    /** 到访核销时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkinTime;

    /** 机构ID */
    private Long tenantId;

    /** 删除标志 */
    private String delFlag;

    public Long getVisitId()
    {
        return visitId;
    }

    public void setVisitId(Long visitId)
    {
        this.visitId = visitId;
    }

    public String getVisitType()
    {
        return visitType;
    }

    public void setVisitType(String visitType)
    {
        this.visitType = visitType;
    }

    public String getVisitorName()
    {
        return visitorName;
    }

    public void setVisitorName(String visitorName)
    {
        this.visitorName = visitorName;
    }

    public String getVisitorPhone()
    {
        return visitorPhone;
    }

    public void setVisitorPhone(String visitorPhone)
    {
        this.visitorPhone = visitorPhone;
    }

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

    public Date getVisitDate()
    {
        return visitDate;
    }

    public void setVisitDate(Date visitDate)
    {
        this.visitDate = visitDate;
    }

    public String getVisitTime()
    {
        return visitTime;
    }

    public void setVisitTime(String visitTime)
    {
        this.visitTime = visitTime;
    }

    public Integer getPeopleCount()
    {
        return peopleCount;
    }

    public void setPeopleCount(Integer peopleCount)
    {
        this.peopleCount = peopleCount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getVisitCode()
    {
        return visitCode;
    }

    public void setVisitCode(String visitCode)
    {
        this.visitCode = visitCode;
    }

    public Date getCheckinTime()
    {
        return checkinTime;
    }

    public void setCheckinTime(Date checkinTime)
    {
        this.checkinTime = checkinTime;
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
