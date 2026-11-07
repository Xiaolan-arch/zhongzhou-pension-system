package com.ruoyi.business.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ruoyi.common.utils.StringUtils;

/**
 * AI 报告客户端装配
 *
 * @author ruoyi
 */
@Configuration
@EnableConfigurationProperties(AiReportProperties.class)
public class AiReportConfig
{
    @Bean
    public AiReportClient aiReportClient(AiReportProperties properties)
    {
        AiReportClient mockClient = new MockAiReportClient();
        if (!"qianfan".equalsIgnoreCase(properties.getProvider()))
        {
            return mockClient;
        }
        if (StringUtils.isEmpty(properties.getQianfan().getApiKey()))
        {
            return mockClient;
        }
        AiReportClient qianfanClient = new QianfanAiReportClient(properties);
        if (properties.isFallbackToMock())
        {
            return new FallbackAiReportClient(qianfanClient, mockClient);
        }
        return qianfanClient;
    }
}
