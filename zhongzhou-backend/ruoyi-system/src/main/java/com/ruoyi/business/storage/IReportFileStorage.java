package com.ruoyi.business.storage;

import java.io.IOException;

/**
 * 报告文件归档存储
 *
 * @author ruoyi
 */
public interface IReportFileStorage
{
    /**
     * 归档文件并返回访问地址
     *
     * @param content 文件内容
     * @param fileName 原始文件名
     * @return 访问地址
     * @throws IOException 写入失败
     */
    public String store(byte[] content, String fileName) throws IOException;
}
