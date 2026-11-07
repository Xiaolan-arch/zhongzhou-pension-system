package com.ruoyi.business.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 服务项目 biz_service_item
 *
 * @author ruoyi
 */
public class ServiceItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 服务项目ID */
    private Long itemId;

    /** 项目名称 */
    private String itemName;

    /** 价格 */
    private BigDecimal price;

    /** 单位 */
    private String unit;

    /** 图片 */
    private String image;

    /** 服务说明 */
    private String description;

    /** 状态（0上架 1下架） */
    private String status;

    /** 机构ID */
    private Long tenantId;

    /** 删除标志 */
    private String delFlag;

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public String getItemName()
    {
        return itemName;
    }

    public void setItemName(String itemName)
    {
        this.itemName = itemName;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
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
