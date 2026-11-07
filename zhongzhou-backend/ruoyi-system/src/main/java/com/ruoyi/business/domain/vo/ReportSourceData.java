package com.ruoyi.business.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import com.ruoyi.business.domain.Checkin;
import com.ruoyi.business.domain.Elder;

/**
 * 报告生成数据源（老人档案 + 位置 + 评估 + 服务/探访/账单统计）
 *
 * @author ruoyi
 */
public class ReportSourceData
{
    /** 老人档案 */
    private Elder elder;

    /** 当前床位位置 */
    private BedLocation location;

    /** 最近一次入住申请（含评估信息） */
    private Checkin checkin;

    /** 周期内服务订单总数 */
    private int serviceTotal;

    /** 周期内已完成服务订单数 */
    private int serviceDone;

    /** 周期内服务金额 */
    private BigDecimal serviceAmount;

    /** 周期内探访次数 */
    private int visitCount;

    /** 未结清账单数 */
    private int unpaidBillCount;

    /** 未结清金额 */
    private BigDecimal unpaidBillAmount;

    /** 已缴费金额 */
    private BigDecimal paidBillAmount;

    /** 统计周期开始 */
    private Date periodStart;

    /** 统计周期结束 */
    private Date periodEnd;

    /** 生成时补充说明 */
    private String extraNote;

    /** 体征聚合指标（AI 报告用，可能为空） */
    private HealthIndicators indicators;

    public Elder getElder()
    {
        return elder;
    }

    public void setElder(Elder elder)
    {
        this.elder = elder;
    }

    public BedLocation getLocation()
    {
        return location;
    }

    public void setLocation(BedLocation location)
    {
        this.location = location;
    }

    public Checkin getCheckin()
    {
        return checkin;
    }

    public void setCheckin(Checkin checkin)
    {
        this.checkin = checkin;
    }

    public int getServiceTotal()
    {
        return serviceTotal;
    }

    public void setServiceTotal(int serviceTotal)
    {
        this.serviceTotal = serviceTotal;
    }

    public int getServiceDone()
    {
        return serviceDone;
    }

    public void setServiceDone(int serviceDone)
    {
        this.serviceDone = serviceDone;
    }

    public BigDecimal getServiceAmount()
    {
        return serviceAmount;
    }

    public void setServiceAmount(BigDecimal serviceAmount)
    {
        this.serviceAmount = serviceAmount;
    }

    public int getVisitCount()
    {
        return visitCount;
    }

    public void setVisitCount(int visitCount)
    {
        this.visitCount = visitCount;
    }

    public int getUnpaidBillCount()
    {
        return unpaidBillCount;
    }

    public void setUnpaidBillCount(int unpaidBillCount)
    {
        this.unpaidBillCount = unpaidBillCount;
    }

    public BigDecimal getUnpaidBillAmount()
    {
        return unpaidBillAmount;
    }

    public void setUnpaidBillAmount(BigDecimal unpaidBillAmount)
    {
        this.unpaidBillAmount = unpaidBillAmount;
    }

    public BigDecimal getPaidBillAmount()
    {
        return paidBillAmount;
    }

    public void setPaidBillAmount(BigDecimal paidBillAmount)
    {
        this.paidBillAmount = paidBillAmount;
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

    public String getExtraNote()
    {
        return extraNote;
    }

    public void setExtraNote(String extraNote)
    {
        this.extraNote = extraNote;
    }

    public HealthIndicators getIndicators()
    {
        return indicators;
    }

    public void setIndicators(HealthIndicators indicators)
    {
        this.indicators = indicators;
    }
}
