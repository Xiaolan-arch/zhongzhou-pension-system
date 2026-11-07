package com.ruoyi.business.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 押金记录业务对象 biz_deposit
 */
public class Deposit extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 押金记录ID */
    private Long depositId;

    /** 老人ID */
    private Long elderId;

    /** 老人姓名 */
    @Excel(name = "老人姓名")
    private String elderName;

    /** 类型（1收押金 2退押金） */
    @Excel(name = "类型", readConverterExp = "1=收押金,2=退押金")
    private String depositType;

    /** 金额 */
    @Excel(name = "金额")
    private BigDecimal amount;

    /** 支付方式（biz_pay_method） */
    @Excel(name = "支付方式")
    private String payMethod;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    @Excel(name = "操作人")
    private String operatorName;

    /** 机构ID */
    private Long tenantId;

    public Long getDepositId()        { return depositId; }
    public void setDepositId(Long v)  { this.depositId = v; }
    public Long getElderId()          { return elderId; }
    public void setElderId(Long v)    { this.elderId = v; }
    public String getElderName()      { return elderName; }
    public void setElderName(String v){ this.elderName = v; }
    public String getDepositType()    { return depositType; }
    public void setDepositType(String v){ this.depositType = v; }
    public BigDecimal getAmount()     { return amount; }
    public void setAmount(BigDecimal v){ this.amount = v; }
    public String getPayMethod()      { return payMethod; }
    public void setPayMethod(String v){ this.payMethod = v; }
    public Long getOperatorId()       { return operatorId; }
    public void setOperatorId(Long v) { this.operatorId = v; }
    public String getOperatorName()   { return operatorName; }
    public void setOperatorName(String v){ this.operatorName = v; }
    public Long getTenantId()         { return tenantId; }
    public void setTenantId(Long v)   { this.tenantId = v; }
}
