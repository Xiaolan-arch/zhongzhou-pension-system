package com.ruoyi.business.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 智能客服配置
 *
 * @author ruoyi
 */
@ConfigurationProperties(prefix = "ai.chat")
public class AiChatProperties
{
    /** 模型提供方（mock/deepseek） */
    private String provider = "mock";

    /** 真实模型调用失败时是否降级为本地模拟 */
    private boolean fallbackToMock = true;

    /** DeepSeek 配置 */
    private DeepSeek deepseek = new DeepSeek();

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public boolean isFallbackToMock()
    {
        return fallbackToMock;
    }

    public void setFallbackToMock(boolean fallbackToMock)
    {
        this.fallbackToMock = fallbackToMock;
    }

    public DeepSeek getDeepseek()
    {
        return deepseek;
    }

    public void setDeepseek(DeepSeek deepseek)
    {
        this.deepseek = deepseek;
    }

    /**
     * DeepSeek（OpenAI 兼容）配置
     */
    public static class DeepSeek
    {
        /** API Key（Bearer 方式） */
        private String apiKey = "";

        /** 对话接口地址 */
        private String endpoint = "https://api.deepseek.com/v1/chat/completions";

        /** 模型名称 */
        private String model = "deepseek-chat";

        /** 温度 */
        private double temperature = 0.7;

        /** 最大输出 token */
        private int maxTokens = 1024;

        /** 超时时间（秒） */
        private int timeoutSeconds = 60;

        public String getApiKey()
        {
            return apiKey;
        }

        public void setApiKey(String apiKey)
        {
            this.apiKey = apiKey;
        }

        public String getEndpoint()
        {
            return endpoint;
        }

        public void setEndpoint(String endpoint)
        {
            this.endpoint = endpoint;
        }

        public String getModel()
        {
            return model;
        }

        public void setModel(String model)
        {
            this.model = model;
        }

        public double getTemperature()
        {
            return temperature;
        }

        public void setTemperature(double temperature)
        {
            this.temperature = temperature;
        }

        public int getMaxTokens()
        {
            return maxTokens;
        }

        public void setMaxTokens(int maxTokens)
        {
            this.maxTokens = maxTokens;
        }

        public int getTimeoutSeconds()
        {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds)
        {
            this.timeoutSeconds = timeoutSeconds;
        }
    }
}
