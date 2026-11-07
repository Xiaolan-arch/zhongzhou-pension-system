package com.ruoyi.business.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 账单明细业务对象 biz_bill_item
 *
 * @author ruoyi
 */
public class BillItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long itemId;

    /** 账单ID */
    private Long billId;

    /** 类型（1加项 2减项） */
    private String itemType;

    /** 费用项目 */
    private String feeName;

    /** 服务内容 */
    private String serviceContent;

    /** 金额（正数） */
    private BigDecimal amount;

    /** 机构ID */
    private Long tenantId;

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public Long getBillId()
    {
        return billId;
    }

    public void setBillId(Long billId)
    {
        this.billId = billId;
    }

    public String getItemType()
    {
        return itemType;
    }

    public void setItemType(String itemType)
    {
        this.itemType = itemType;
    }

    public String getFeeName()
    {
        return feeName;
    }

    public void setFeeName(String feeName)
    {
        this.feeName = feeName;
    }

    public String getServiceContent()
    {
        return serviceContent;
    }

    public void setServiceContent(String serviceContent)
    {
        this.serviceContent = serviceContent;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public Long getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(Long tenantId)
    {
        this.tenantId = tenantId;
    }
}
