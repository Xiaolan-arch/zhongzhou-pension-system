package com.ruoyi.web.controller.business;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.HealthReport;
import com.ruoyi.business.domain.vo.ReportExport;
import com.ruoyi.business.service.IHealthReportService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * AI 健康报告 控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/report")
public class HealthReportController extends BaseController
{
    @Autowired
    private IHealthReportService healthReportService;

    /**
     * 查询报告列表
     */
    @PreAuthorize("@ss.hasPermi('business:report:list')")
    @GetMapping("/list")
    public TableDataInfo list(HealthReport report)
    {
        startPage();
        List<HealthReport> list = healthReportService.selectHealthReportList(report);
        return getDataTable(list);
    }

    /**
     * 查询报告详情
     */
    @PreAuthorize("@ss.hasPermi('business:report:list')")
    @GetMapping("/{reportId}")
    public AjaxResult getInfo(@PathVariable Long reportId)
    {
        HealthReport report = healthReportService.selectHealthReportById(reportId);
        if (report == null)
        {
            return AjaxResult.error("报告不存在或无权访问");
        }
        return success(report);
    }

    /**
     * 生成健康报告
     */
    @PreAuthorize("@ss.hasPermi('business:report:generate')")
    @Log(title = "AI健康报告", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    public AjaxResult generate(@RequestBody HealthReport report)
    {
        HealthReport generated = healthReportService.generateReport(report.getElderId(), report.getReportType(),
                report.getPeriodStart(), report.getPeriodEnd(), report.getRemark());
        return success(generated);
    }

    /**
     * 基于历史报告重新生成
     */
    @PreAuthorize("@ss.hasPermi('business:report:generate')")
    @Log(title = "AI健康报告", businessType = BusinessType.INSERT)
    @PostMapping("/regenerate/{reportId}")
    public AjaxResult regenerate(@PathVariable Long reportId)
    {
        HealthReport generated = healthReportService.regenerateReport(reportId);
        return success(generated);
    }

    /**
     * 删除报告
     */
    @PreAuthorize("@ss.hasPermi('business:report:remove')")
    @Log(title = "AI健康报告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{reportIds}")
    public AjaxResult remove(@PathVariable Long[] reportIds)
    {
        return toAjax(healthReportService.deleteHealthReportByIds(reportIds));
    }

    /**
     * 导出 Word 报告
     */
    @PreAuthorize("@ss.hasPermi('business:report:export')")
    @Log(title = "AI健康报告", businessType = BusinessType.EXPORT)
    @GetMapping("/export/{reportId}")
    public void export(@PathVariable Long reportId, HttpServletResponse response)
    {
        ReportExport export = healthReportService.exportDocx(reportId);
        try
        {
            String fileName = URLEncoder.encode(export.getFileName(), "UTF-8").replace("+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + "; filename*=UTF-8''" + fileName);
            response.getOutputStream().write(export.getContent());
            response.getOutputStream().flush();
        }
        catch (IOException e)
        {
            logger.error("报告导出失败", e);
        }
    }
}
