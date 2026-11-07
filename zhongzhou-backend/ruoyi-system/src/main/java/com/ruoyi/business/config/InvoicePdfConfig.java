package com.ruoyi.business.config;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ruoyi.business.util.InvoicePdfGenerator;

/**
 * 发票 PDF 中文字体配置
 *
 * 从 application.yml 的 report.font.path 读取字体路径，
 * 启动时注入 InvoicePdfGenerator 的静态字体路径。
 */
@Component
public class InvoicePdfConfig
{
    @Value("${report.font.path:C:/Windows/Fonts/simhei.ttf}")
    private String fontPath;

    @PostConstruct
    public void init()
    {
        InvoicePdfGenerator.setFontPath(fontPath);
    }
}
