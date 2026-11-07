package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * AI客服聊天消息 biz_chat_message
 *
 * @author ruoyi
 */
public class ChatMessage
{
    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private Long msgId;

    /** 会话ID */
    private Long sessionId;

    /** 角色（user/assistant/system） */
    private String role;

    /** 消息内容 */
    private String content;

    /** 消耗token数 */
    private Integer tokens;

    /** 发送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getMsgId()            { return msgId; }
    public void setMsgId(Long v)      { this.msgId = v; }
    public Long getSessionId()        { return sessionId; }
    public void setSessionId(Long v)  { this.sessionId = v; }
    public String getRole()           { return role; }
    public void setRole(String v)     { this.role = v; }
    public String getContent()        { return content; }
    public void setContent(String v)  { this.content = v; }
    public Integer getTokens()        { return tokens; }
    public void setTokens(Integer v)  { this.tokens = v; }
    public Date getCreateTime()       { return createTime; }
    public void setCreateTime(Date v) { this.createTime = v; }
}
