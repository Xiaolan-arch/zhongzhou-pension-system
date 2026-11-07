package com.ruoyi.business.audit;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 业务审计日志业务对象 biz_audit_log
 */
public class BizAuditLog
{
    /** 日志ID */
    private Long logId;

    /** 业务类型（BILL_PAY / BILL_REFUND / CHECKIN / CHECKOUT） */
    private String bizType;

    /** 业务主键 */
    private Long bizId;

    /** 动作描述 */
    private String action;

    /** 变更前快照（可为空） */
    private String beforeJson;

    /** 变更后快照（可为空） */
    private String afterJson;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 机构ID */
    private Long tenantId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 请求参数（用于 beginTime / endTime 时间范围查询） */
    private Map<String, Object> params;

    public Map<String, Object> getParams()
    {
        if (params == null)
        {
            params = new HashMap<String, Object>();
        }
        return params;
    }

    public void setParams(Map<String, Object> params)
    {
        this.params = params;
    }

    public Long getLogId()             { return logId; }
    public void setLogId(Long v)       { this.logId = v; }
    public String getBizType()         { return bizType; }
    public void setBizType(String v)   { this.bizType = v; }
    public Long getBizId()             { return bizId; }
    public void setBizId(Long v)       { this.bizId = v; }
    public String getAction()          { return action; }
    public void setAction(String v)    { this.action = v; }
    public String getBeforeJson()      { return beforeJson; }
    public void setBeforeJson(String v){ this.beforeJson = v; }
    public String getAfterJson()       { return afterJson; }
    public void setAfterJson(String v) { this.afterJson = v; }
    public Long getOperatorId()        { return operatorId; }
    public void setOperatorId(Long v)  { this.operatorId = v; }
    public String getOperatorName()    { return operatorName; }
    public void setOperatorName(String v){ this.operatorName = v; }
    public Long getTenantId()          { return tenantId; }
    public void setTenantId(Long v)    { this.tenantId = v; }
    public Date getCreateTime()        { return createTime; }
    public void setCreateTime(Date v)  { this.createTime = v; }
}
