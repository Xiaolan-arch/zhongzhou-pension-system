package com.ruoyi.business.service;

import java.util.Date;
import java.util.List;
import com.ruoyi.business.domain.HealthReport;
import com.ruoyi.business.domain.vo.ReportExport;

/**
 * AI 健康报告 服务层
 *
 * @author ruoyi
 */
public interface IHealthReportService
{
    /**
     * 查询报告列表
     */
    public List<HealthReport> selectHealthReportList(HealthReport report);

    /**
     * 查询报告详情
     */
    public HealthReport selectHealthReportById(Long reportId);

    /**
     * 生成报告
     *
     * @param elderId 老人ID
     * @param reportType 报告类型
     * @param periodStart 周期开始
     * @param periodEnd 周期结束
     * @param extraNote 补充说明
     * @return 生成的报告
     */
    public HealthReport generateReport(Long elderId, String reportType, Date periodStart, Date periodEnd,
            String extraNote);

    /**
     * 基于历史报告重新生成
     */
    public HealthReport regenerateReport(Long reportId);

    /**
     * 批量逻辑删除报告
     */
    public int deleteHealthReportByIds(Long[] reportIds);

    /**
     * 导出 Word 报告
     */
    public ReportExport exportDocx(Long reportId);
}
