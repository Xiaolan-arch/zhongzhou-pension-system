package com.ruoyi.business.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 微信订阅消息发送记录 biz_message_log
 */
public class MessageLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 机构ID */
    private Long tenantId;

    /** 家属ID */
    private Long familyId;

    /** 老人ID */
    private Long elderId;

    /** 老人姓名 */
    @Excel(name = "老人姓名")
    private String elderName;

    /** 家属手机号 */
    @Excel(name = "家属手机号")
    private String phone;

    /** 微信 openid */
    private String openid;

    /** 消息类型（1账单到期 2逾期提醒 3缴费成功） */
    @Excel(name = "消息类型", readConverterExp = "1=账单到期,2=逾期提醒,3=缴费成功")
    private String msgType;

    /** 消息标题 */
    @Excel(name = "消息标题")
    private String title;

    /** 消息内容 */
    @Excel(name = "消息内容")
    private String content;

    /** 发送状态（0成功 1失败 2跳过） */
    @Excel(name = "发送状态", readConverterExp = "0=成功,1=失败,2=跳过")
    private String sendStatus;

    /** 错误信息/说明 */
    private String errorMsg;

    public Long getLogId()          { return logId; }
    public void setLogId(Long v)    { this.logId = v; }
    public Long getTenantId()       { return tenantId; }
    public void setTenantId(Long v) { this.tenantId = v; }
    public Long getFamilyId()       { return familyId; }
    public void setFamilyId(Long v) { this.familyId = v; }
    public Long getElderId()        { return elderId; }
    public void setElderId(Long v)  { this.elderId = v; }
    public String getElderName()    { return elderName; }
    public void setElderName(String v) { this.elderName = v; }
    public String getPhone()        { return phone; }
    public void setPhone(String v)  { this.phone = v; }
    public String getOpenid()       { return openid; }
    public void setOpenid(String v) { this.openid = v; }
    public String getMsgType()      { return msgType; }
    public void setMsgType(String v){ this.msgType = v; }
    public String getTitle()        { return title; }
    public void setTitle(String v)  { this.title = v; }
    public String getContent()      { return content; }
    public void setContent(String v){ this.content = v; }
    public String getSendStatus()   { return sendStatus; }
    public void setSendStatus(String v) { this.sendStatus = v; }
    public String getErrorMsg()     { return errorMsg; }
    public void setErrorMsg(String v) { this.errorMsg = v; }
}
