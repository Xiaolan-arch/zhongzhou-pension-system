package com.ruoyi.business.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;

/**
 * 阿里云 OSS 归档
 *
 * @author ruoyi
 */
public class OssReportFileStorage implements IReportFileStorage
{
    private final ReportStorageProperties properties;

    public OssReportFileStorage(ReportStorageProperties properties)
    {
        this.properties = properties;
    }

    @Override
    public String store(byte[] content, String fileName) throws IOException
    {
        ReportStorageProperties.Oss oss = properties.getOss();
        String day = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String key = oss.getPrefix() + properties.getDir() + "/" + day + "/"
                + UUID.randomUUID().toString().replace("-", "") + suffix(fileName);
        OSS client = new OSSClientBuilder().build(oss.getEndpoint(), oss.getAccessKeyId(), oss.getAccessKeySecret());
        try
        {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(content.length);
            metadata.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            client.putObject(oss.getBucket(), key, new ByteArrayInputStream(content), metadata);
            Date expiration = new Date(System.currentTimeMillis() + oss.getExpireSeconds() * 1000L);
            return client.generatePresignedUrl(oss.getBucket(), key, expiration).toString();
        }
        catch (Exception e)
        {
            throw new IOException("OSS 归档失败：" + e.getMessage(), e);
        }
        finally
        {
            client.shutdown();
        }
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
