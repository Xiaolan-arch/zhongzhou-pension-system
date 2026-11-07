package com.ruoyi.business.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 床位护理员关联 biz_bed_nurse
 *
 * @author ruoyi
 */
public class BedNurse extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 床位ID */
    private Long bedId;

    /** 护理员ID */
    private Long nurseId;

    /** 机构ID */
    private Long tenantId;

    /** 护理员姓名（展示字段） */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String nurseName;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getBedId()
    {
        return bedId;
    }

    public void setBedId(Long bedId)
    {
        this.bedId = bedId;
    }

    public Long getNurseId()
    {
        return nurseId;
    }

    public void setNurseId(Long nurseId)
    {
        this.nurseId = nurseId;
    }

    public Long getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(Long tenantId)
    {
        this.tenantId = tenantId;
    }

    public String getNurseName()
    {
        return nurseName;
    }

    public void setNurseName(String nurseName)
    {
        this.nurseName = nurseName;
    }
}
