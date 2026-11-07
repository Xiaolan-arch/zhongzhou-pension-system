package com.ruoyi.business.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 护理员 biz_nurse
 *
 * @author ruoyi
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Nurse extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 护理员ID */
    private Long nurseId;

    /** 姓名 */
    private String nurseName;

    /** 电话 */
    private String phone;

    /** 状态（0正常 1停用） */
    private String status;

    /** 机构ID */
    private Long tenantId;

    /** 删除标志 */
    private String delFlag;

    public Long getNurseId()
    {
        return nurseId;
    }

    public void setNurseId(Long nurseId)
    {
        this.nurseId = nurseId;
    }

    public String getNurseName()
    {
        return nurseName;
    }

    public void setNurseName(String nurseName)
    {
        this.nurseName = nurseName;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
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

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }
}
