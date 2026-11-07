package com.ruoyi.business.util;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.ruoyi.business.domain.Bill;
import com.ruoyi.business.domain.BillItem;
import com.ruoyi.business.domain.PaymentRecord;

/**
 * 发票 PDF 生成工具（iText 5.x + Windows 系统中文字体）
 *
 * 演示说明：依赖 C:\Windows\Fonts\simhei.ttf 支持中文渲染，
 * 部署到 Linux 时需替换字体路径（如 /usr/share/fonts/truetype/wqy-microhei.ttc）
 *
 * @author ruoyi
 */
public class InvoicePdfGenerator
{
    /** 中文字体路径，由 InvoicePdfConfig 从 application.yml 的 report.font.path 注入 */
    private static String fontPath = "C:/Windows/Fonts/simhei.ttf";

    /** 配置注入入口（Spring 启动时调用一次） */
    public static void setFontPath(String path)
    {
        if (path != null && !path.trim().isEmpty())
        {
            fontPath = path.trim();
        }
    }

    /**
     * 生成发票 PDF 字节流（账单 + 明细 + 收款历史）
     */
    public static byte[] generate(Bill bill, List<BillItem> items, List<PaymentRecord> records) throws Exception
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 60, 60);
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = buildFont(20, Font.BOLD, BaseColor.BLACK);
        Font headFont = buildFont(13, Font.BOLD, new BaseColor(47, 155, 106));
        Font normalFont = buildFont(11, Font.NORMAL, BaseColor.BLACK);
        Font smallFont = buildFont(9, Font.NORMAL, new BaseColor(120, 120, 120));

        // 标题
        Paragraph title = new Paragraph("中 州 养 老 发 票", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(6);
        document.add(title);

        // 发票编号 + 打印时间
        Paragraph headerInfo = new Paragraph(
                String.format("发票编号：%-16s  打印时间：%s",
                        safe(bill.getBillNo()),
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())),
                smallFont);
        headerInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(headerInfo);

        addHr(document);

        // 基本信息表格
        PdfPTable infoTable = new PdfPTable(4);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[] { 1.2f, 3f, 1.2f, 3f });
        infoTable.addCell(labelCell("老人姓名", headFont));
        infoTable.addCell(valueCell(safe(bill.getElderName()), normalFont));
        infoTable.addCell(labelCell("账单月份", headFont));
        infoTable.addCell(valueCell(safe(bill.getBillMonth()), normalFont));
        infoTable.addCell(labelCell("账单周期", headFont));
        infoTable.addCell(valueCell(fmtDate(bill.getPeriodStart()) + " ~ " + fmtDate(bill.getPeriodEnd()), normalFont));
        infoTable.addCell(labelCell("账单编号", headFont));
        infoTable.addCell(valueCell(safe(bill.getBillNo()), normalFont));
        document.add(infoTable);

        addHr(document);

        // 金额汇总
        PdfPTable amountTable = new PdfPTable(4);
        amountTable.setWidthPercentage(100);
        amountTable.setWidths(new float[] { 2.5f, 2f, 2.5f, 2f });
        amountTable.addCell(labelCell("账单金额", headFont));
        amountTable.addCell(valueCell(fmtMoney(bill.getAmount()), normalFont));
        amountTable.addCell(labelCell("已收金额", headFont));
        amountTable.addCell(valueCell(fmtMoney(bill.getPaidAmount()), normalFont));
        amountTable.addCell(labelCell("待收金额", headFont));
        BigDecimal pending = (bill.getAmount() == null ? BigDecimal.ZERO : bill.getAmount())
                .subtract(bill.getPaidAmount() == null ? BigDecimal.ZERO : bill.getPaidAmount());
        PdfPCell pendingCell = valueCell(fmtMoney(pending), normalFont);
        if (pending.compareTo(BigDecimal.ZERO) > 0)
        {
            pendingCell.setPhrase(new Phrase(fmtMoney(pending), buildFont(11, Font.BOLD, BaseColor.RED)));
        }
        amountTable.addCell(pendingCell);
        amountTable.addCell(labelCell("支付状态", headFont));
        amountTable.addCell(valueCell(payStatusText(bill.getPayStatus()), normalFont));
        document.add(amountTable);

        addHr(document);

        // 账单明细
        if (items != null && !items.isEmpty())
        {
            Paragraph section1 = new Paragraph("账单明细", headFont);
            section1.setSpacingBefore(6);
            section1.setSpacingAfter(4);
            document.add(section1);

            PdfPTable itemTable = new PdfPTable(4);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[] { 1f, 3f, 2f, 2f });
            addHeaderRow(itemTable, "序号", "费用项目", "金额", "备注");
            int i = 1;
            for (BillItem item : items)
            {
                itemTable.addCell(String.valueOf(i++));
                itemTable.addCell(safe(item.getFeeName()));
                itemTable.addCell(fmtMoney(item.getAmount()));
                itemTable.addCell(safe(item.getServiceContent()));
            }
            document.add(itemTable);
        }

        // 收款历史
        Paragraph section2 = new Paragraph("收款历史", headFont);
        section2.setSpacingBefore(10);
        section2.setSpacingAfter(4);
        document.add(section2);

        PdfPTable recordTable = new PdfPTable(5);
        recordTable.setWidthPercentage(100);
        recordTable.setWidths(new float[] { 1.5f, 2f, 2.5f, 2f, 2.5f });
        addHeaderRow(recordTable, "单号", "时间", "金额", "方式", "备注");
        if (records != null && !records.isEmpty())
        {
            for (PaymentRecord r : records)
            {
                recordTable.addCell(safe(r.getRecordNo()));
                recordTable.addCell(fmtDateTime(r.getCreateTime()));
                recordTable.addCell(fmtMoney(r.getPayAmount()));
                recordTable.addCell(safe(r.getPayMethod()));
                recordTable.addCell(safe(r.getRemark()));
            }
        }
        else
        {
            PdfPCell empty = new PdfPCell(new Phrase("（暂无收款记录）", smallFont));
            empty.setColspan(5);
            empty.setHorizontalAlignment(Element.ALIGN_CENTER);
            empty.setPadding(8);
            recordTable.addCell(empty);
        }
        document.add(recordTable);

        // 页脚
        Paragraph footer = new Paragraph("本发票由系统自动生成，请核对后作为财务凭证保存。", smallFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(18);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    // ==================== 辅助 ====================

    private static Font buildFont(int size, int style, BaseColor color) throws Exception
    {
        BaseFont bf;
        try
        {
            bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        }
        catch (Exception e)
        {
            // 回退：iText 内置 Helvetica 不支持中文，乱码但保证程序不崩
            bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        }
        return new Font(bf, size, style, color);
    }

    private static PdfPCell labelCell(String text, Font font)
    {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new BaseColor(238, 245, 242));
        cell.setPadding(6);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private static PdfPCell valueCell(String text, Font font)
    {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private static void addHeaderRow(PdfPTable table, String... headers) throws Exception
    {
        Font whiteFont = buildFont(11, Font.BOLD, BaseColor.WHITE);
        for (String h : headers)
        {
            PdfPCell cell = new PdfPCell(new Phrase(h, whiteFont));
            cell.setBackgroundColor(new BaseColor(47, 155, 106));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private static void addHr(Document document) throws DocumentException
    {
        Paragraph hr = new Paragraph();
        hr.add(new com.itextpdf.text.Chunk("──────────────────────────────────────────────"));
        hr.setAlignment(Element.ALIGN_CENTER);
        hr.setSpacingBefore(4);
        hr.setSpacingAfter(4);
        document.add(hr);
    }

    private static String safe(Object o)
    {
        return o == null ? "" : o.toString();
    }

    private static String fmtMoney(BigDecimal v)
    {
        if (v == null)
        {
            return "¥0.00";
        }
        return "¥" + v.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    private static String fmtDate(Date d)
    {
        if (d == null)
        {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    private static String fmtDateTime(Date d)
    {
        if (d == null)
        {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(d);
    }

    private static String payStatusText(String status)
    {
        if (status == null)
        {
            return "未知";
        }
        switch (status)
        {
            case "0": return "未收款";
            case "1": return "已付清";
            case "2": return "已退款";
            case "3": return "部分已收";
            default: return "状态:" + status;
        }
    }
}
