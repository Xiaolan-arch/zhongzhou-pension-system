package com.ruoyi.business.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI客服聊天会话 biz_chat_session
 *
 * @author ruoyi
 */
public class ChatSession extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 会话ID */
    private Long sessionId;

    /** 机构ID */
    private Long tenantId;

    /** 家属ID */
    private Long familyId;

    /** 当前咨询老人ID */
    private Long elderId;

    /** 会话标题（首条消息截断） */
    private String title;

    /** 删除标志 */
    private String delFlag;

    public Long getSessionId()         { return sessionId; }
    public void setSessionId(Long v)   { this.sessionId = v; }
    public Long getTenantId()          { return tenantId; }
    public void setTenantId(Long v)    { this.tenantId = v; }
    public Long getFamilyId()          { return familyId; }
    public void setFamilyId(Long v)    { this.familyId = v; }
    public Long getElderId()           { return elderId; }
    public void setElderId(Long v)     { this.elderId = v; }
    public String getTitle()           { return title; }
    public void setTitle(String v)     { this.title = v; }
    public String getDelFlag()         { return delFlag; }
    public void setDelFlag(String v)   { this.delFlag = v; }
}
