package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 护理任务 biz_service_task
 *
 * @author ruoyi
 */
public class ServiceTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 关联服务订单ID */
    private Long orderId;

    /** 关联订单号 */
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

    /** 服务日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date serviceDate;

    /** 计划执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date planTime;

    /** 任务状态（0待执行 1已执行 2已完成 3已取消 4已逾期） */
    private String taskStatus;

    /** 执行反馈（完成/取消时填写） */
    private String feedback;

    /** 机构ID */
    private Long tenantId;

    /** 删除标志 */
    private String delFlag;

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

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

    public Date getServiceDate()
    {
        return serviceDate;
    }

    public void setServiceDate(Date serviceDate)
    {
        this.serviceDate = serviceDate;
    }

    public Date getPlanTime()
    {
        return planTime;
    }

    public void setPlanTime(Date planTime)
    {
        this.planTime = planTime;
    }

    public String getTaskStatus()
    {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus)
    {
        this.taskStatus = taskStatus;
    }

    public String getFeedback()
    {
        return feedback;
    }

    public void setFeedback(String feedback)
    {
        this.feedback = feedback;
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
