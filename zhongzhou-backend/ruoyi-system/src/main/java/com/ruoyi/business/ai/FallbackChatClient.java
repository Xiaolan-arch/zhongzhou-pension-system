package com.ruoyi.business.ai;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 带降级能力的智能客服客户端：真实模型调用失败时自动回退本地模拟
 *
 * @author ruoyi
 */
public class FallbackChatClient implements ChatClient
{
    private static final Logger log = LoggerFactory.getLogger(FallbackChatClient.class);

    private final ChatClient primary;

    private final ChatClient fallback;

    public FallbackChatClient(ChatClient primary, ChatClient fallback)
    {
        this.primary = primary;
        this.fallback = fallback;
    }

    @Override
    public String chat(String systemPrompt, List<ChatMessageVO> history, String userContent)
    {
        try
        {
            return primary.chat(systemPrompt, history, userContent);
        }
        catch (Exception e)
        {
            log.warn("AI 客服真实模型调用失败，降级为本地模拟：{}", e.getMessage());
            return fallback.chat(systemPrompt, history, userContent);
        }
    }
}
