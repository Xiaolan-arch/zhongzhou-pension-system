package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI 健康报告业务对象 biz_health_report
 *
 * @author ruoyi
 */
public class HealthReport extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 报告ID */
    private Long reportId;

    /** 老人ID */
    private Long elderId;

    /** 老人姓名 */
    private String elderName;

    /** 报告类型（0综合照护 1健康评估 2月度总结） */
    private String reportType;

    /** 报告标题 */
    private String reportTitle;

    /** 统计周期开始 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date periodStart;

    /** 统计周期结束 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date periodEnd;

    /** 健康评分（0-100） */
    private Integer score;

    /** 风险等级（0良好 1关注 2预警） */
    private String riskLevel;

    /** 摘要 */
    private String summary;

    /** 报告正文（Markdown） */
    private String content;

    /** 模型来源（mock/qianfan） */
    private String modelSource;

    /** 模型名称 */
    private String modelName;

    /** 状态（0生成中 1已生成 2生成失败） */
    private String status;

    /** 失败原因 */
    private String errorMsg;

    /** 归档文件地址 */
    private String archivedUrl;

    /** 机构ID */
    private Long tenantId;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    public Long getReportId()
    {
        return reportId;
    }

    public void setReportId(Long reportId)
    {
        this.reportId = reportId;
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

    public String getReportType()
    {
        return reportType;
    }

    public void setReportType(String reportType)
    {
        this.reportType = reportType;
    }

    public String getReportTitle()
    {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle)
    {
        this.reportTitle = reportTitle;
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

    public Integer getScore()
    {
        return score;
    }

    public void setScore(Integer score)
    {
        this.score = score;
    }

    public String getRiskLevel()
    {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel)
    {
        this.riskLevel = riskLevel;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getModelSource()
    {
        return modelSource;
    }

    public void setModelSource(String modelSource)
    {
        this.modelSource = modelSource;
    }

    public String getModelName()
    {
        return modelName;
    }

    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    public String getArchivedUrl()
    {
        return archivedUrl;
    }

    public void setArchivedUrl(String archivedUrl)
    {
        this.archivedUrl = archivedUrl;
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
