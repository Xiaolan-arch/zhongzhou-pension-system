package com.ruoyi.business.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ruoyi.common.utils.StringUtils;

/**
 * AI 智能客服客户端装配
 *
 * @author ruoyi
 */
@Configuration
@EnableConfigurationProperties(AiChatProperties.class)
public class AiChatConfig
{
    @Bean
    public ChatClient chatClient(AiChatProperties properties)
    {
        ChatClient mockClient = new MockChatClient();
        if (!"deepseek".equalsIgnoreCase(properties.getProvider()))
        {
            return mockClient;
        }
        if (StringUtils.isEmpty(properties.getDeepseek().getApiKey()))
        {
            return mockClient;
        }
        ChatClient deepSeekClient = new DeepSeekChatClient(properties);
        if (properties.isFallbackToMock())
        {
            return new FallbackChatClient(deepSeekClient, mockClient);
        }
        return deepSeekClient;
    }
}
