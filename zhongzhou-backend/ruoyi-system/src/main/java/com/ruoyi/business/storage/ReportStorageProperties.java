package com.ruoyi.business.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 报告归档存储配置（本地 / 阿里云 OSS 可切换）
 *
 * @author ruoyi
 */
@ConfigurationProperties(prefix = "report.storage")
public class ReportStorageProperties
{
    /** 是否归档导出文件 */
    private boolean enabled = true;

    /** 存储类型（local/oss） */
    private String type = "local";

    /** 归档目录 */
    private String dir = "report";

    /** OSS 配置 */
    private Oss oss = new Oss();

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getDir()
    {
        return dir;
    }

    public void setDir(String dir)
    {
        this.dir = dir;
    }

    public Oss getOss()
    {
        return oss;
    }

    public void setOss(Oss oss)
    {
        this.oss = oss;
    }

    /**
     * 阿里云 OSS 配置
     */
    public static class Oss
    {
        /** 访问域名，如 oss-cn-hangzhou.aliyuncs.com */
        private String endpoint = "";

        /** 存储空间 */
        private String bucket = "";

        /** AccessKey ID */
        private String accessKeyId = "";

        /** AccessKey Secret */
        private String accessKeySecret = "";

        /** 对象前缀 */
        private String prefix = "zhongzhou/report/";

        /** 签名链接有效期（秒） */
        private int expireSeconds = 3600;

        public String getEndpoint()
        {
            return endpoint;
        }

        public void setEndpoint(String endpoint)
        {
            this.endpoint = endpoint;
        }

        public String getBucket()
        {
            return bucket;
        }

        public void setBucket(String bucket)
        {
            this.bucket = bucket;
        }

        public String getAccessKeyId()
        {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId)
        {
            this.accessKeyId = accessKeyId;
        }

        public String getAccessKeySecret()
        {
            return accessKeySecret;
        }

        public void setAccessKeySecret(String accessKeySecret)
        {
            this.accessKeySecret = accessKeySecret;
        }

        public String getPrefix()
        {
            return prefix;
        }

        public void setPrefix(String prefix)
        {
            this.prefix = prefix;
        }

        public int getExpireSeconds()
        {
            return expireSeconds;
        }

        public void setExpireSeconds(int expireSeconds)
        {
            this.expireSeconds = expireSeconds;
        }
    }
}
