package com.ruoyi.business.ai;

import java.util.List;

/**
 * AI 智能客服客户端
 *
 * @author ruoyi
 */
public interface ChatClient
{
    /**
     * 多轮对话
     *
     * @param systemPrompt 系统提示词（含业务上下文）
     * @param history      历史消息（不含 system 与本次用户消息）
     * @param userContent  本次用户消息
     * @return AI 回复文本
     */
    public String chat(String systemPrompt, List<ChatMessageVO> history, String userContent);
}
