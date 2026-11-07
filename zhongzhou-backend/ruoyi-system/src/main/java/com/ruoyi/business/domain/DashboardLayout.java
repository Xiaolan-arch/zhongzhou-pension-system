package com.ruoyi.business.domain;

import java.util.Date;

/**
 * 工作台个性化布局
 *
 * @author ruoyi
 */
public class DashboardLayout
{
    /** 布局ID */
    private Long layoutId;

    /** 用户ID */
    private Long userId;

    /** 机构ID */
    private Long tenantId;

    /** 布局配置JSON（区块顺序与显隐） */
    private String layoutJson;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    public Long getLayoutId()
    {
        return layoutId;
    }

    public void setLayoutId(Long layoutId)
    {
        this.layoutId = layoutId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(Long tenantId)
    {
        this.tenantId = tenantId;
    }

    public String getLayoutJson()
    {
        return layoutJson;
    }

    public void setLayoutJson(String layoutJson)
    {
        this.layoutJson = layoutJson;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }
}
