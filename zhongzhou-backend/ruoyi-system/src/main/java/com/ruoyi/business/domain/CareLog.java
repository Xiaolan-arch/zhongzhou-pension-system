package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 护理日志 biz_care_log
 *
 * @author ruoyi
 */
public class CareLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 老人ID */
    private Long elderId;

    /** 老人姓名 */
    private String elderName;

    /** 机构ID */
    private Long tenantId;

    /** 日志日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date logDate;

    /** 类型（0日常照护 1饮食 2清洁 3康复 4心理关怀） */
    private String careType;

    /** 护理内容 */
    private String content;

    /** 护理员 */
    private String nurseName;

    /** 删除标志 */
    private String delFlag;

    public Long getLogId()
    {
        return logId;
    }

    public void setLogId(Long logId)
    {
        this.logId = logId;
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

    public Long getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(Long tenantId)
    {
        this.tenantId = tenantId;
    }

    public Date getLogDate()
    {
        return logDate;
    }

    public void setLogDate(Date logDate)
    {
        this.logDate = logDate;
    }

    public String getCareType()
    {
        return careType;
    }

    public void setCareType(String careType)
    {
        this.careType = careType;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getNurseName()
    {
        return nurseName;
    }

    public void setNurseName(String nurseName)
    {
        this.nurseName = nurseName;
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
