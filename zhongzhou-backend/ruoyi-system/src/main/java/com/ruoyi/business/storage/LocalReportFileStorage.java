package com.ruoyi.business.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;

/**
 * 本地磁盘归档（默认）
 *
 * @author ruoyi
 */
public class LocalReportFileStorage implements IReportFileStorage
{
    private final ReportStorageProperties properties;

    public LocalReportFileStorage(ReportStorageProperties properties)
    {
        this.properties = properties;
    }

    @Override
    public String store(byte[] content, String fileName) throws IOException
    {
        String day = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String relative = properties.getDir() + "/" + day + "/" + UUID.randomUUID().toString().replace("-", "")
                + suffix(fileName);
        File target = new File(RuoYiConfig.getProfile(), relative);
        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs())
        {
            throw new IOException("创建归档目录失败：" + parent.getAbsolutePath());
        }
        Files.write(target.toPath(), content);
        return Constants.RESOURCE_PREFIX + "/" + relative.replace('\\', '/');
    }

    private String suffix(String fileName)
    {
        if (fileName == null)
        {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        return index >= 0 ? fileName.substring(index) : "";
    }
}
