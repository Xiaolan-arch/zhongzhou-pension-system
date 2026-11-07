package com.ruoyi.business.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ruoyi.common.utils.StringUtils;

/**
 * 报告归档存储装配：默认本地，配置完整时切换 OSS
 *
 * @author ruoyi
 */
@Configuration
@EnableConfigurationProperties(ReportStorageProperties.class)
public class ReportStorageConfig
{
    private static final Logger log = LoggerFactory.getLogger(ReportStorageConfig.class);

    @Bean
    public IReportFileStorage reportFileStorage(ReportStorageProperties properties)
    {
        if ("oss".equalsIgnoreCase(properties.getType()))
        {
            ReportStorageProperties.Oss oss = properties.getOss();
            if (StringUtils.isNotEmpty(oss.getEndpoint()) && StringUtils.isNotEmpty(oss.getBucket())
                    && StringUtils.isNotEmpty(oss.getAccessKeyId())
                    && StringUtils.isNotEmpty(oss.getAccessKeySecret()))
            {
                return new OssReportFileStorage(properties);
            }
            log.warn("OSS 配置不完整，报告归档自动降级为本地存储");
        }
        return new LocalReportFileStorage(properties);
    }
}
