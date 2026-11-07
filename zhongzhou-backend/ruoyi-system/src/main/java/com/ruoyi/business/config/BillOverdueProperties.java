package com.ruoyi.business.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 逾期账单滞纳金配置（bill.overdue）
 */
@Component
@ConfigurationProperties(prefix = "bill.overdue")
public class BillOverdueProperties
{
    /** 是否启用滞纳金计算 */
    private boolean enabled = true;

    /** 宽限期（账单周期结束后多少天开始计滞纳金） */
    private int graceDays = 30;

    /** 日费率（按未缴金额计算，0.0005 = 每日万分之五） */
    private BigDecimal dailyRate = new BigDecimal("0.0005");

    public boolean isEnabled()        { return enabled; }
    public void setEnabled(boolean v) { this.enabled = v; }
    public int getGraceDays()         { return graceDays; }
    public void setGraceDays(int v)   { this.graceDays = v; }
    public BigDecimal getDailyRate()  { return dailyRate; }
    public void setDailyRate(BigDecimal v) { this.dailyRate = v; }
}
