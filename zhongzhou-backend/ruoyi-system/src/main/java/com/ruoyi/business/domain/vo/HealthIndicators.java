package com.ruoyi.business.domain.vo;

import java.math.BigDecimal;

/**
 * AI 报告使用的老人体征聚合指标
 * （从 biz_health_record 最近 N 条记录里计算出来，方便 PromptBuilder 格式化输出）
 */
public class HealthIndicators
{
    /** 参与聚合的体征记录条数 */
    private int recordsCount;

    /** 统计天数 */
    private int periodDays;

    /** 异常天数（存在 is_abnormal='1' 的记录的天数） */
    private int abnormalDays;

    /* --- 血压 收缩压 --- */
    private BigDecimal bpHighAvg;
    private Integer bpHighMax;
    private Integer bpHighMin;

    /* --- 血压 舒张压 --- */
    private BigDecimal bpLowAvg;
    private Integer bpLowMax;
    private Integer bpLowMin;

    /* --- 心率 --- */
    private BigDecimal hrAvg;
    private Integer hrMax;
    private Integer hrMin;

    /* --- 血糖 --- */
    private BigDecimal sugarAvg;
    private BigDecimal sugarMax;

    /* --- 血氧 --- */
    private BigDecimal oxygenAvg;
    private BigDecimal oxygenMin;

    /* --- 最新一条 --- */
    private Integer latestBpHigh;
    private Integer latestBpLow;
    private Integer latestHr;
    private BigDecimal latestBodyTemp;
    private BigDecimal latestSugar;
    private BigDecimal latestOxygen;
    private String latestDate;

    /** 综合风险提示（根据 abnormalDays + 极值自动生成，PromptBuilder 可直接拼接） */
    private String overallRiskHint;

    public int getRecordsCount()          { return recordsCount; }
    public void setRecordsCount(int v)    { this.recordsCount = v; }
    public int getPeriodDays()            { return periodDays; }
    public void setPeriodDays(int v)      { this.periodDays = v; }
    public int getAbnormalDays()          { return abnormalDays; }
    public void setAbnormalDays(int v)    { this.abnormalDays = v; }

    public BigDecimal getBpHighAvg()      { return bpHighAvg; }
    public void setBpHighAvg(BigDecimal v){ this.bpHighAvg = v; }
    public Integer getBpHighMax()         { return bpHighMax; }
    public void setBpHighMax(Integer v)   { this.bpHighMax = v; }
    public Integer getBpHighMin()         { return bpHighMin; }
    public void setBpHighMin(Integer v)   { this.bpHighMin = v; }

    public BigDecimal getBpLowAvg()       { return bpLowAvg; }
    public void setBpLowAvg(BigDecimal v) { this.bpLowAvg = v; }
    public Integer getBpLowMax()          { return bpLowMax; }
    public void setBpLowMax(Integer v)    { this.bpLowMax = v; }
    public Integer getBpLowMin()          { return bpLowMin; }
    public void setBpLowMin(Integer v)    { this.bpLowMin = v; }

    public BigDecimal getHrAvg()          { return hrAvg; }
    public void setHrAvg(BigDecimal v)    { this.hrAvg = v; }
    public Integer getHrMax()             { return hrMax; }
    public void setHrMax(Integer v)       { this.hrMax = v; }
    public Integer getHrMin()             { return hrMin; }
    public void setHrMin(Integer v)       { this.hrMin = v; }

    public BigDecimal getSugarAvg()       { return sugarAvg; }
    public void setSugarAvg(BigDecimal v) { this.sugarAvg = v; }
    public BigDecimal getSugarMax()       { return sugarMax; }
    public void setSugarMax(BigDecimal v) { this.sugarMax = v; }

    public BigDecimal getOxygenAvg()      { return oxygenAvg; }
    public void setOxygenAvg(BigDecimal v){ this.oxygenAvg = v; }
    public BigDecimal getOxygenMin()     { return oxygenMin; }
    public void setOxygenMin(BigDecimal v){ this.oxygenMin = v; }

    public Integer getLatestBpHigh()      { return latestBpHigh; }
    public void setLatestBpHigh(Integer v){ this.latestBpHigh = v; }
    public Integer getLatestBpLow()       { return latestBpLow; }
    public void setLatestBpLow(Integer v) { this.latestBpLow = v; }
    public Integer getLatestHr()          { return latestHr; }
    public void setLatestHr(Integer v)    { this.latestHr = v; }
    public BigDecimal getLatestBodyTemp() { return latestBodyTemp; }
    public void setLatestBodyTemp(BigDecimal v){ this.latestBodyTemp = v; }
    public BigDecimal getLatestSugar()    { return latestSugar; }
    public void setLatestSugar(BigDecimal v) { this.latestSugar = v; }
    public BigDecimal getLatestOxygen()   { return latestOxygen; }
    public void setLatestOxygen(BigDecimal v){ this.latestOxygen = v; }
    public String getLatestDate()         { return latestDate; }
    public void setLatestDate(String v)   { this.latestDate = v; }

    public String getOverallRiskHint()    { return overallRiskHint; }
    public void setOverallRiskHint(String v) { this.overallRiskHint = v; }
}
