package com.ruoyi.business.ai;

/**
 * 聊天消息模型（role + content），供 LLM 客户端组装多轮对话
 *
 * @author ruoyi
 */
public class ChatMessageVO
{
    private String role;

    private String content;

    public ChatMessageVO()
    {
    }

    public ChatMessageVO(String role, String content)
    {
        this.role = role;
        this.content = content;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }
}
