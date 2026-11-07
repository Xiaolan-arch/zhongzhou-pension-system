package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 账单业务对象 biz_bill
 *
 * @author ruoyi
 */
public class Bill extends BaseEntity
{
    private static final long serialVersionUID = 1L;

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

    /** 关联入住申请ID */
    private Long checkinId;

    /** 账单月份 */
    @Excel(name = "账单月份")
    private String billMonth;

    /** 账单周期开始 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "周期开始", dateFormat = "yyyy-MM-dd")
    private Date periodStart;

    /** 账单周期结束 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "周期结束", dateFormat = "yyyy-MM-dd")
    private Date periodEnd;

    /** 账单金额 */
    @Excel(name = "账单金额")
    private BigDecimal amount;

    /** 押金 */
    @Excel(name = "押金")
    private BigDecimal deposit;

    /** 预交款 */
    @Excel(name = "预交款")
    private BigDecimal prepaid;

    /** 已付金额 */
    @Excel(name = "已付金额")
    private BigDecimal paidAmount;

    /** 支付状态（0待支付 1已支付 2已退款） */
    @Excel(name = "支付状态", readConverterExp = "0=待支付,1=已支付,2=已退款")
    private String payStatus;

    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "支付时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    /** 支付方式（biz_pay_method） */
    @Excel(name = "支付方式")
    private String payMethod;

    /** 支付凭证 */
    private String payVoucher;

    /** 账单类型（0首期 1月度 2退住结算） */
    @Excel(name = "账单类型", readConverterExp = "0=首期,1=月度,2=退住结算")
    private String billType;

    /** 滞纳金（逾期未缴金额按日费率累计） */
    @Excel(name = "滞纳金")
    private BigDecimal penaltyAmount;

    /** 逾期天数（非数据库字段，按周期结束+宽限期计算） */
    private Integer overdueDays;

    /** 机构ID */
    private Long tenantId;

    /** 乐观锁：更新前的已付金额（非数据库字段） */
    private transient BigDecimal oldPaidAmount;

    /** 乐观锁：更新前的支付状态（非数据库字段） */
    private transient String oldPayStatus;

    /** 本次收款金额（前端传入，非数据库字段，支持部分收款） */
    private transient BigDecimal payAmount;

    /** 月度费用（从 biz_checkin.fee_standard JOIN 而来，非数据库字段，仅用于 generateBill 取费） */
    private transient BigDecimal monthlyFee;

    /** 本次退款金额（前端传入，非数据库字段，支持部分退款） */
    private transient BigDecimal refundAmount;

    public Long getBillId()
    {
        return billId;
    }

    public void setBillId(Long billId)
    {
        this.billId = billId;
    }

    public String getBillNo()
    {
        return billNo;
    }

    public void setBillNo(String billNo)
    {
        this.billNo = billNo;
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

    public Long getCheckinId()
    {
        return checkinId;
    }

    public void setCheckinId(Long checkinId)
    {
        this.checkinId = checkinId;
    }

    public String getBillMonth()
    {
        return billMonth;
    }

    public void setBillMonth(String billMonth)
    {
        this.billMonth = billMonth;
    }

    public Date getPeriodStart()
    {
        return periodStart;
    }

    public void setPeriodStart(Date periodStart)
    {
        this.periodStart = periodStart;
    }

    public Date getPeriodEnd()
    {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd)
    {
        this.periodEnd = periodEnd;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public BigDecimal getDeposit()
    {
        return deposit;
    }

    public void setDeposit(BigDecimal deposit)
    {
        this.deposit = deposit;
    }

    public BigDecimal getPrepaid()
    {
        return prepaid;
    }

    public void setPrepaid(BigDecimal prepaid)
    {
        this.prepaid = prepaid;
    }

    public BigDecimal getPaidAmount()
    {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount)
    {
        this.paidAmount = paidAmount;
    }

    public String getPayStatus()
    {
        return payStatus;
    }

    public void setPayStatus(String payStatus)
    {
        this.payStatus = payStatus;
    }

    public Date getPayTime()
    {
        return payTime;
    }

    public void setPayTime(Date payTime)
    {
        this.payTime = payTime;
    }

    public String getPayMethod()
    {
        return payMethod;
    }

    public void setPayMethod(String payMethod)
    {
        this.payMethod = payMethod;
    }

    public String getPayVoucher()
    {
        return payVoucher;
    }

    public void setPayVoucher(String payVoucher)
    {
        this.payVoucher = payVoucher;
    }

    public String getBillType()
    {
        return billType;
    }

    public void setBillType(String billType)
    {
        this.billType = billType;
    }

    public BigDecimal getPenaltyAmount()
    {
        return penaltyAmount;
    }

    public void setPenaltyAmount(BigDecimal penaltyAmount)
    {
        this.penaltyAmount = penaltyAmount;
    }

    public Integer getOverdueDays()
    {
        return overdueDays;
    }

    public void setOverdueDays(Integer overdueDays)
    {
        this.overdueDays = overdueDays;
    }

    public Long getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(Long tenantId)
    {
        this.tenantId = tenantId;
    }

    public BigDecimal getOldPaidAmount() { return oldPaidAmount; }
    public void setOldPaidAmount(BigDecimal v) { this.oldPaidAmount = v; }
    public String getOldPayStatus() { return oldPayStatus; }
    public void setOldPayStatus(String v) { this.oldPayStatus = v; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal v) { this.payAmount = v; }
    public BigDecimal getMonthlyFee() { return monthlyFee; }
    public void setMonthlyFee(BigDecimal v) { this.monthlyFee = v; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal v) { this.refundAmount = v; }
}
