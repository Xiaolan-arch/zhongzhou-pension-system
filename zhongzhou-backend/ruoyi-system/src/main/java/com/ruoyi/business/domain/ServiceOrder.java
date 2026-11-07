package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 服务订单 biz_service_order
 *
 * @author ruoyi
 */
public class ServiceOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 订单号 */
    private String orderNo;

    /** 老人ID */
    private Long elderId;

    /** 老人姓名 */
    private String elderName;

    /** 服务项目ID */
    private Long itemId;

    /** 项目名称 */
    private String itemName;

    /** 护理员ID */
    private Long nurseId;

    /** 护理员姓名 */
    private String nurseName;

    /** 服务时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date serviceTime;

    /** 金额 */
    private BigDecimal amount;

    /** 状态（0待执行 1已执行 2已完成 3已取消） */
    private String status;

    /** 签到时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkinTime;

    /** 签到操作人 */
    private String checkinOperator;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    /** 完成操作人 */
    private String finishOperator;

    /** 服务记录/备注 */
    private String serviceRemark;

    /** 关联护理任务ID（家属端展示用） */
    private Long taskId;

    /** 护理任务状态（家属端展示用） */
    private String taskStatus;

    /** 护理任务执行反馈（家属端展示用） */
    private String taskFeedback;

    /** 机构ID */
    private Long tenantId;

    /** 删除标志 */
    private String delFlag;

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
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

    public Date getServiceTime()
    {
        return serviceTime;
    }

    public void setServiceTime(Date serviceTime)
    {
        this.serviceTime = serviceTime;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getCheckinTime()         { return checkinTime; }
    public void setCheckinTime(Date v)    { this.checkinTime = v; }
    public String getCheckinOperator()   { return checkinOperator; }
    public void setCheckinOperator(String v) { this.checkinOperator = v; }
    public Date getFinishTime()          { return finishTime; }
    public void setFinishTime(Date v)    { this.finishTime = v; }
    public String getFinishOperator()    { return finishOperator; }
    public void setFinishOperator(String v) { this.finishOperator = v; }
    public String getServiceRemark()     { return serviceRemark; }
    public void setServiceRemark(String v) { this.serviceRemark = v; }
    public Long getTaskId()              { return taskId; }
    public void setTaskId(Long v)        { this.taskId = v; }
    public String getTaskStatus()        { return taskStatus; }
    public void setTaskStatus(String v)  { this.taskStatus = v; }
    public String getTaskFeedback()      { return taskFeedback; }
    public void setTaskFeedback(String v){ this.taskFeedback = v; }

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
