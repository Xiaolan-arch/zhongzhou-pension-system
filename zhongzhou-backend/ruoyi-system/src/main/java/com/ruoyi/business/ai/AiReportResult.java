package com.ruoyi.business.ai;

/**
 * AI 报告生成结果
 *
 * @author ruoyi
 */
public class AiReportResult
{
    /** 报告正文（Markdown） */
    private String content;

    /** 模型来源（mock/qianfan） */
    private String modelSource;

    /** 模型名称 */
    private String modelName;

    public AiReportResult()
    {
    }

    public AiReportResult(String content, String modelSource, String modelName)
    {
        this.content = content;
        this.modelSource = modelSource;
        this.modelName = modelName;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getModelSource()
    {
        return modelSource;
    }

    public void setModelSource(String modelSource)
    {
        this.modelSource = modelSource;
    }

    public String getModelName()
    {
        return modelName;
    }

    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }
}
