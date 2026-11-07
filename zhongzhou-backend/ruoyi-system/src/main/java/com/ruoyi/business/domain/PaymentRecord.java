package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 收款记录业务对象 biz_payment_record
 */
public class PaymentRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 收款记录ID */
    private Long recordId;

    /** 收款单号 */
    @Excel(name = "收款单号")
    private String recordNo;

    /** 账单ID */
    private Long billId;

    /** 账单编号 */
    @Excel(name = "账单编号")
    private String billNo;

    /** 老人ID */
    private Long elderId;

    /** 老人姓名 */
    @Excel(name = "老人姓名")
    private String elderName;

    /** 本次收款金额 */
    @Excel(name = "收款金额")
    private BigDecimal payAmount;

    /** 支付方式 */
    @Excel(name = "支付方式")
    private String payMethod;

    /** 支付凭证 */
    private String payVoucher;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    @Excel(name = "操作人")
    private String operatorName;

    /** 机构ID */
    private Long tenantId;

    public Long getRecordId()        { return recordId; }
    public void setRecordId(Long v)  { this.recordId = v; }
    public String getRecordNo()      { return recordNo; }
    public void setRecordNo(String v){ this.recordNo = v; }
    public Long getBillId()          { return billId; }
    public void setBillId(Long v)    { this.billId = v; }
    public String getBillNo()        { return billNo; }
    public void setBillNo(String v)  { this.billNo = v; }
    public Long getElderId()         { return elderId; }
    public void setElderId(Long v)   { this.elderId = v; }
    public String getElderName()     { return elderName; }
    public void setElderName(String v){ this.elderName = v; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal v){ this.payAmount = v; }
    public String getPayMethod()     { return payMethod; }
    public void setPayMethod(String v){ this.payMethod = v; }
    public String getPayVoucher()    { return payVoucher; }
    public void setPayVoucher(String v){ this.payVoucher = v; }
    public Long getOperatorId()      { return operatorId; }
    public void setOperatorId(Long v){ this.operatorId = v; }
    public String getOperatorName()  { return operatorName; }
    public void setOperatorName(String v){ this.operatorName = v; }
    public Long getTenantId()        { return tenantId; }
    public void setTenantId(Long v)  { this.tenantId = v; }
}
