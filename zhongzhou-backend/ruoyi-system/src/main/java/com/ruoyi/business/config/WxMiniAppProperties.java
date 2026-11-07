package com.ruoyi.business.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序订阅消息配置（wx.miniapp）
 */
@Component
@ConfigurationProperties(prefix = "wx.miniapp")
public class WxMiniAppProperties
{
    /** 是否启用真实微信接口调用（false 时仅记录日志） */
    private boolean enabled = false;

    /** Mock 模式：只落库发送记录，不真实调用微信接口（演示默认开启） */
    private boolean mock = true;

    /** 小程序 AppID */
    private String appId = "";

    /** 小程序 AppSecret */
    private String appSecret = "";

    /** 账单到期提醒模板ID */
    private String billDueTemplate = "";

    /** 逾期账单提醒模板ID */
    private String overdueTemplate = "";

    public boolean isEnabled()          { return enabled; }
    public void setEnabled(boolean v)   { this.enabled = v; }
    public boolean isMock()             { return mock; }
    public void setMock(boolean v)      { this.mock = v; }
    public String getAppId()            { return appId; }
    public void setAppId(String v)      { this.appId = v; }
    public String getAppSecret()        { return appSecret; }
    public void setAppSecret(String v)  { this.appSecret = v; }
    public String getBillDueTemplate()  { return billDueTemplate; }
    public void setBillDueTemplate(String v) { this.billDueTemplate = v; }
    public String getOverdueTemplate()  { return overdueTemplate; }
    public void setOverdueTemplate(String v) { this.overdueTemplate = v; }
}
