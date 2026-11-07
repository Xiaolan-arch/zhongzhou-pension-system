package com.ruoyi.business.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import com.ruoyi.business.domain.HealthReport;
import com.ruoyi.common.utils.StringUtils;

/**
 * 报告 Word 导出（Markdown 简化排版）
 *
 * @author ruoyi
 */
public class ReportDocxExporter
{
    /**
     * 生成 docx 字节流
     */
    public static byte[] export(HealthReport report) throws IOException
    {
        XWPFDocument document = new XWPFDocument();
        try
        {
            addTitle(document, report.getReportTitle());
            addMeta(document, "老人姓名：" + report.getElderName());
            addMeta(document, "统计周期：" + format(report.getPeriodStart()) + " 至 " + format(report.getPeriodEnd()));
            addMeta(document, "健康评分：" + (report.getScore() == null ? "-" : report.getScore()) + " 分   风险等级："
                    + riskName(report.getRiskLevel()));
            addMeta(document, "生成时间：" + formatDateTime(report.getCreateTime()) + "   生成模型："
                    + (StringUtils.isEmpty(report.getModelName()) ? "-" : report.getModelName()));
            paragraph(document, "摘要：" + (StringUtils.isEmpty(report.getSummary()) ? "-" : report.getSummary()), false, 11);
            paragraph(document, "", false, 6);

            String content = report.getContent() == null ? "" : report.getContent();
            for (String line : content.split("\\r?\\n"))
            {
                appendMarkdownLine(document, line);
            }
            paragraph(document, "", false, 6);
            paragraph(document, "说明：本报告由 AI 生成，仅供机构照护与家属沟通参考，不作为医疗诊断依据。", false, 9);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return out.toByteArray();
        }
        finally
        {
            document.close();
        }
    }

    private static void appendMarkdownLine(XWPFDocument document, String line)
    {
        String text = line == null ? "" : line.trim();
        if (text.isEmpty())
        {
            paragraph(document, "", false, 6);
            return;
        }
        if (text.startsWith("#"))
        {
            int level = 0;
            while (level < text.length() && text.charAt(level) == '#')
            {
                level++;
            }
            String heading = stripInline(text.substring(level).trim());
            int size = level <= 1 ? 15 : (level == 2 ? 13 : 12);
            paragraph(document, heading, true, size);
            return;
        }
        if (text.startsWith("- ") || text.startsWith("* "))
        {
            paragraph(document, "• " + stripInline(text.substring(2)), false, 11);
            return;
        }
        paragraph(document, stripInline(text), false, 11);
    }

    private static void addTitle(XWPFDocument document, String title)
    {
        XWPFParagraph p = document.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = p.createRun();
        run.setText(title == null ? "AI 健康报告" : title);
        run.setBold(true);
        run.setFontSize(18);
        run.setFontFamily("微软雅黑");
    }

    private static void addMeta(XWPFDocument document, String text)
    {
        paragraph(document, text, false, 10);
    }

    private static void paragraph(XWPFDocument document, String text, boolean bold, int fontSize)
    {
        XWPFParagraph p = document.createParagraph();
        p.setSpacingAfter(4);
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontSize(fontSize);
        run.setFontFamily("微软雅黑");
    }

    private static String stripInline(String text)
    {
        return text.replace("**", "").replace("`", "");
    }

    private static String riskName(String riskLevel)
    {
        if ("1".equals(riskLevel))
        {
            return "关注";
        }
        if ("2".equals(riskLevel))
        {
            return "预警";
        }
        return "良好";
    }

    private static String format(Date date)
    {
        return date == null ? "-" : new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private static String formatDateTime(Date date)
    {
        return date == null ? "-" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
