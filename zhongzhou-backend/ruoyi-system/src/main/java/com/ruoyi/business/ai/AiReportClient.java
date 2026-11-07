package com.ruoyi.business.ai;

import com.ruoyi.business.domain.vo.ReportSourceData;

/**
 * AI 报告生成客户端
 *
 * @author ruoyi
 */
public interface AiReportClient
{
    /**
     * 生成报告正文
     *
     * @param source 报告数据源
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @return 生成结果
     */
    public AiReportResult generate(ReportSourceData source, String systemPrompt, String userPrompt);
}
