package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 老人体征记录对象 biz_health_record
 */
public class HealthRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 老人ID */
    @Excel(name = "老人ID")
    private Long elderId;

    /** 老人姓名（冗余展示） */
    private String elderName;

    /** 测量日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "测量日期", dateFormat = "yyyy-MM-dd")
    private Date recordDate;

    /** 测量时间 HH:mm */
    @Excel(name = "测量时间")
    private String recordTime;

    /** 收缩压（高压） mmHg */
    @Excel(name = "收缩压")
    private Integer bloodPressureHigh;

    /** 舒张压（低压） mmHg */
    @Excel(name = "舒张压")
    private Integer bloodPressureLow;

    /** 心率 次/分 */
    @Excel(name = "心率")
    private Integer heartRate;

    /** 体温 ℃ */
    @Excel(name = "体温")
    private BigDecimal bodyTemp;

    /** 血糖 mmol/L */
    @Excel(name = "血糖")
    private BigDecimal bloodSugar;

    /** 血氧 % */
    @Excel(name = "血氧")
    private BigDecimal bloodOxygen;

    /** 体重 kg */
    @Excel(name = "体重")
    private BigDecimal weight;

    /** 步数 */
    @Excel(name = "步数")
    private Integer steps;

    /** 睡眠时长 h */
    @Excel(name = "睡眠时长")
    private BigDecimal sleepHours;

    /** 是否异常（0正常 1异常） */
    @Excel(name = "是否异常", readConverterExp = "0=正常,1=异常")
    private String isAbnormal;

    /** 异常项 */
    private String abnormalItems;

    /** 机构ID */
    private Long tenantId;

    /** 删除标志 */
    private String delFlag;

    public Long getRecordId()                 { return recordId; }
    public void setRecordId(Long recordId)   { this.recordId = recordId; }
    public Long getElderId()                  { return elderId; }
    public void setElderId(Long elderId)      { this.elderId = elderId; }
    public String getElderName()              { return elderName; }
    public void setElderName(String v)        { this.elderName = v; }
    public Date getRecordDate()               { return recordDate; }
    public void setRecordDate(Date recordDate){ this.recordDate = recordDate; }
    public String getRecordTime()             { return recordTime; }
    public void setRecordTime(String recordTime) { this.recordTime = recordTime; }
    public Integer getBloodPressureHigh()     { return bloodPressureHigh; }
    public void setBloodPressureHigh(Integer v){ this.bloodPressureHigh = v; }
    public Integer getBloodPressureLow()      { return bloodPressureLow; }
    public void setBloodPressureLow(Integer v) { this.bloodPressureLow = v; }
    public Integer getHeartRate()             { return heartRate; }
    public void setHeartRate(Integer v)       { this.heartRate = v; }
    public BigDecimal getBodyTemp()           { return bodyTemp; }
    public void setBodyTemp(BigDecimal v)     { this.bodyTemp = v; }
    public BigDecimal getBloodSugar()         { return bloodSugar; }
    public void setBloodSugar(BigDecimal v)   { this.bloodSugar = v; }
    public BigDecimal getBloodOxygen()        { return bloodOxygen; }
    public void setBloodOxygen(BigDecimal v)  { this.bloodOxygen = v; }
    public BigDecimal getWeight()             { return weight; }
    public void setWeight(BigDecimal v)       { this.weight = v; }
    public Integer getSteps()                 { return steps; }
    public void setSteps(Integer v)           { this.steps = v; }
    public BigDecimal getSleepHours()         { return sleepHours; }
    public void setSleepHours(BigDecimal v)   { this.sleepHours = v; }
    public String getIsAbnormal()             { return isAbnormal; }
    public void setIsAbnormal(String v)       { this.isAbnormal = v; }
    public String getAbnormalItems()         { return abnormalItems; }
    public void setAbnormalItems(String v)    { this.abnormalItems = v; }
    public Long getTenantId()                 { return tenantId; }
    public void setTenantId(Long tenantId)    { this.tenantId = tenantId; }
    public String getDelFlag()                { return delFlag; }
    public void setDelFlag(String delFlag)    { this.delFlag = delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", recordId)
            .append("elderId", elderId)
            .append("recordDate", recordDate)
            .append("bloodPressureHigh", bloodPressureHigh)
            .append("bloodPressureLow", bloodPressureLow)
            .append("heartRate", heartRate)
            .append("bodyTemp", bodyTemp)
            .append("bloodSugar", bloodSugar)
            .append("bloodOxygen", bloodOxygen)
            .append("weight", weight)
            .append("steps", steps)
            .append("sleepHours", sleepHours)
            .append("isAbnormal", isAbnormal)
            .toString();
    }
}
