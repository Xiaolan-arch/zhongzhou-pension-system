package com.ruoyi.business.domain.vo;

/**
 * 报告导出结果
 *
 * @author ruoyi
 */
public class ReportExport
{
    /** 文件名 */
    private String fileName;

    /** 文件内容 */
    private byte[] content;

    /** 归档地址 */
    private String archivedUrl;

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public byte[] getContent()
    {
        return content;
    }

    public void setContent(byte[] content)
    {
        this.content = content;
    }

    public String getArchivedUrl()
    {
        return archivedUrl;
    }

    public void setArchivedUrl(String archivedUrl)
    {
        this.archivedUrl = archivedUrl;
    }
}
