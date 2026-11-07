package com.ruoyi.business.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.business.domain.vo.ReportSourceData;

/**
 * 大模型调用失败时降级为本地模拟
 *
 * @author ruoyi
 */
public class FallbackAiReportClient implements AiReportClient
{
    private static final Logger log = LoggerFactory.getLogger(FallbackAiReportClient.class);

    private final AiReportClient primary;

    private final AiReportClient fallback;

    public FallbackAiReportClient(AiReportClient primary, AiReportClient fallback)
    {
        this.primary = primary;
        this.fallback = fallback;
    }

    @Override
    public AiReportResult generate(ReportSourceData source, String systemPrompt, String userPrompt)
    {
        try
        {
            return primary.generate(source, systemPrompt, userPrompt);
        }
        catch (Exception e)
        {
            log.warn("AI 报告主模型调用失败，降级为本地模拟：{}", e.getMessage());
            return fallback.generate(source, systemPrompt, userPrompt);
        }
    }
}
