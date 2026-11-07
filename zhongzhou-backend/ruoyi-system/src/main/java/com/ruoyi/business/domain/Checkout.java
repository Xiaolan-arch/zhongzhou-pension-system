package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 退住申请业务对象 biz_checkout
 *
 * @author ruoyi
 */
public class Checkout extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 退住申请ID */
    private Long checkoutId;

    /** 申请单号 */
    private String applyNo;

    /** 老人ID */
    private Long elderId;

    /** 老人姓名 */
    private String elderName;

    /** 退住日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date checkoutDate;

    /** 退住原因 */
    private String reason;

    /** 状态（0申请退住 1待申请审批 2待解除合同 3待调整账单 4待账单审批 5待退住审批 6待费用清算 7已完成 9已驳回） */
    private String applyStatus;

    /** 申请审批结果 */
    private String audit1Result;

    /** 申请审批意见 */
    private String audit1Opinion;

    /** 申请审批人 */
    private String audit1By;

    /** 申请审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date audit1Time;

    /** 解除合同日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date terminateDate;

    /** 解除合同文件 */
    private String terminateFile;

    /** 调整金额 */
    private BigDecimal adjustAmount;

    /** 调整说明 */
    private String adjustRemark;

    /** 账单审批结果 */
    private String audit2Result;

    /** 账单审批意见 */
    private String audit2Opinion;

    /** 账单审批人 */
    private String audit2By;

    /** 账单审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date audit2Time;

    /** 退住审批结果 */
    private String audit3Result;

    /** 退住审批意见 */
    private String audit3Opinion;

    /** 退住审批人 */
    private String audit3By;

    /** 退住审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date audit3Time;

    /** 结算金额 */
    private BigDecimal settleAmount;

    /** 退款方式 */
    private String refundMethod;

    /** 清算时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date settleTime;

    /** 机构ID */
    private Long tenantId;

    public Long getCheckoutId()
    {
        return checkoutId;
    }

    public void setCheckoutId(Long checkoutId)
    {
        this.checkoutId = checkoutId;
    }

    public String getApplyNo()
    {
        return applyNo;
    }

    public void setApplyNo(String applyNo)
    {
        this.applyNo = applyNo;
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

    public Date getCheckoutDate()
    {
        return checkoutDate;
    }

    public void setCheckoutDate(Date checkoutDate)
    {
        this.checkoutDate = checkoutDate;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public String getApplyStatus()
    {
        return applyStatus;
    }

    public void setApplyStatus(String applyStatus)
    {
        this.applyStatus = applyStatus;
    }

    public String getAudit1Result()
    {
        return audit1Result;
    }

    public void setAudit1Result(String audit1Result)
    {
        this.audit1Result = audit1Result;
    }

    public String getAudit1Opinion()
    {
        return audit1Opinion;
    }

    public void setAudit1Opinion(String audit1Opinion)
    {
        this.audit1Opinion = audit1Opinion;
    }

    public String getAudit1By()
    {
        return audit1By;
    }

    public void setAudit1By(String audit1By)
    {
        this.audit1By = audit1By;
    }

    public Date getAudit1Time()
    {
        return audit1Time;
    }

    public void setAudit1Time(Date audit1Time)
    {
        this.audit1Time = audit1Time;
    }

    public Date getTerminateDate()
    {
        return terminateDate;
    }

    public void setTerminateDate(Date terminateDate)
    {
        this.terminateDate = terminateDate;
    }

    public String getTerminateFile()
    {
        return terminateFile;
    }

    public void setTerminateFile(String terminateFile)
    {
        this.terminateFile = terminateFile;
    }

    public BigDecimal getAdjustAmount()
    {
        return adjustAmount;
    }

    public void setAdjustAmount(BigDecimal adjustAmount)
    {
        this.adjustAmount = adjustAmount;
    }

    public String getAdjustRemark()
    {
        return adjustRemark;
    }

    public void setAdjustRemark(String adjustRemark)
    {
        this.adjustRemark = adjustRemark;
    }

    public String getAudit2Result()
    {
        return audit2Result;
    }

    public void setAudit2Result(String audit2Result)
    {
        this.audit2Result = audit2Result;
    }

    public String getAudit2Opinion()
    {
        return audit2Opinion;
    }

    public void setAudit2Opinion(String audit2Opinion)
    {
        this.audit2Opinion = audit2Opinion;
    }

    public String getAudit2By()
    {
        return audit2By;
    }

    public void setAudit2By(String audit2By)
    {
        this.audit2By = audit2By;
    }

    public Date getAudit2Time()
    {
        return audit2Time;
    }

    public void setAudit2Time(Date audit2Time)
    {
        this.audit2Time = audit2Time;
    }

    public String getAudit3Result()
    {
        return audit3Result;
    }

    public void setAudit3Result(String audit3Result)
    {
        this.audit3Result = audit3Result;
    }

    public String getAudit3Opinion()
    {
        return audit3Opinion;
    }

    public void setAudit3Opinion(String audit3Opinion)
    {
        this.audit3Opinion = audit3Opinion;
    }

    public String getAudit3By()
    {
        return audit3By;
    }

    public void setAudit3By(String audit3By)
    {
        this.audit3By = audit3By;
    }

    public Date getAudit3Time()
    {
        return audit3Time;
    }

    public void setAudit3Time(Date audit3Time)
    {
        this.audit3Time = audit3Time;
    }

    public BigDecimal getSettleAmount()
    {
        return settleAmount;
    }

    public void setSettleAmount(BigDecimal settleAmount)
    {
        this.settleAmount = settleAmount;
    }

    public String getRefundMethod()
    {
        return refundMethod;
    }

    public void setRefundMethod(String refundMethod)
    {
        this.refundMethod = refundMethod;
    }

    public Date getSettleTime()
    {
        return settleTime;
    }

    public void setSettleTime(Date settleTime)
    {
        this.settleTime = settleTime;
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
