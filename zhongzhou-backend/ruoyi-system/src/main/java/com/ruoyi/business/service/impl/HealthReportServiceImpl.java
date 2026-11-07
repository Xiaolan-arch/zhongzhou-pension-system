package com.ruoyi.business.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.ai.AiReportClient;
import com.ruoyi.business.ai.AiReportProperties;
import com.ruoyi.business.ai.AiReportResult;
import com.ruoyi.business.ai.HealthReportEvaluator;
import com.ruoyi.business.ai.HealthReportPromptBuilder;
import com.ruoyi.business.domain.Elder;
import com.ruoyi.business.domain.HealthRecord;
import com.ruoyi.business.domain.HealthReport;
import com.ruoyi.business.domain.vo.BedLocation;
import com.ruoyi.business.domain.vo.HealthIndicators;
import com.ruoyi.business.domain.vo.ReportExport;
import com.ruoyi.business.domain.vo.ReportSourceData;
import com.ruoyi.business.mapper.ElderMapper;
import com.ruoyi.business.mapper.HealthRecordMapper;
import com.ruoyi.business.mapper.HealthReportMapper;
import com.ruoyi.business.service.IHealthReportService;
import com.ruoyi.business.storage.IReportFileStorage;
import com.ruoyi.business.storage.ReportStorageProperties;
import com.ruoyi.business.util.ReportDocxExporter;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * AI 健康报告 服务层处理
 *
 * @author ruoyi
 */
@Service
public class HealthReportServiceImpl implements IHealthReportService
{
    private static final Logger log = LoggerFactory.getLogger(HealthReportServiceImpl.class);

    /** 统计周期最大天数 */
    private static final int MAX_PERIOD_DAYS = 365;

    /** 摘要最大长度 */
    private static final int SUMMARY_LENGTH = 160;

    @Autowired
    private HealthReportMapper healthReportMapper;

    @Autowired
    private ElderMapper elderMapper;

    @Autowired
    private HealthRecordMapper healthRecordMapper;

    @Autowired
    private AiReportClient aiReportClient;

    @Autowired
    private AiReportProperties aiReportProperties;

    @Autowired
    private IReportFileStorage reportFileStorage;

    @Autowired
    private ReportStorageProperties reportStorageProperties;

    @Override
    public List<HealthReport> selectHealthReportList(HealthReport report)
    {
        return healthReportMapper.selectHealthReportList(report);
    }

    @Override
    public HealthReport selectHealthReportById(Long reportId)
    {
        return healthReportMapper.selectHealthReportById(reportId);
    }

    @Override
    public HealthReport generateReport(Long elderId, String reportType, Date periodStart, Date periodEnd,
            String extraNote)
    {
        if (elderId == null)
        {
            throw new ServiceException("请选择需要生成报告的老人");
        }
        Elder elder = elderMapper.selectElderById(elderId);
        if (elder == null)
        {
            throw new ServiceException("老人档案不存在或无权访问");
        }
        if (!"0".equals(elder.getStatus()))
        {
            throw new ServiceException("仅可为在住老人生成健康报告");
        }
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null)
        {
            throw new ServiceException("机构信息异常");
        }

        String type = normalizeReportType(reportType);
        Date end = periodEnd == null ? today() : periodEnd;
        Date start = periodStart == null ? addDays(end, -29) : periodStart;
        if (start.after(end))
        {
            throw new ServiceException("统计周期开始日期不能晚于结束日期");
        }
        if (daysBetween(start, end) > MAX_PERIOD_DAYS)
        {
            throw new ServiceException("统计周期不能超过 " + MAX_PERIOD_DAYS + " 天");
        }

        ReportSourceData source = buildSourceData(elder, start, end, extraNote);
        String systemPrompt = HealthReportPromptBuilder.buildSystemPrompt();
        String userPrompt = HealthReportPromptBuilder.buildUserPrompt(source, type);

        HealthReport report = new HealthReport();
        report.setElderId(elder.getElderId());
        report.setElderName(elder.getElderName());
        report.setReportType(type);
        report.setReportTitle(elder.getElderName() + " · " + HealthReportPromptBuilder.reportTypeName(type));
        report.setPeriodStart(start);
        report.setPeriodEnd(end);
        report.setStatus("1");
        report.setTenantId(tenantId);
        report.setCreateBy(SecurityUtils.getUsername());
        report.setRemark(extraNote);

        AiReportResult result;
        try
        {
            result = aiReportClient.generate(source, systemPrompt, userPrompt);
            if (result == null || StringUtils.isEmpty(result.getContent()))
            {
                throw new ServiceException("AI 报告内容为空");
            }
        }
        catch (Exception e)
        {
            saveFailedReport(report, e);
            if (e instanceof ServiceException)
            {
                throw (ServiceException) e;
            }
            throw new ServiceException("报告生成失败：" + e.getMessage());
        }

        HealthReportEvaluator.Evaluation evaluation = HealthReportEvaluator.evaluate(source);
        report.setContent(result.getContent());
        report.setScore(evaluation.getScore());
        report.setRiskLevel(evaluation.getRiskLevel());
        report.setSummary(summarize(result.getContent()));
        report.setModelSource(StringUtils.isEmpty(result.getModelSource()) ? "mock" : result.getModelSource());
        report.setModelName(StringUtils.isEmpty(result.getModelName()) ? "本地规则模版" : result.getModelName());
        healthReportMapper.insertHealthReport(report);
        return report;
    }

    @Override
    public HealthReport regenerateReport(Long reportId)
    {
        HealthReport current = healthReportMapper.selectHealthReportById(reportId);
        if (current == null)
        {
            throw new ServiceException("报告不存在或无权访问");
        }
        return generateReport(current.getElderId(), current.getReportType(), current.getPeriodStart(),
                current.getPeriodEnd(), current.getRemark());
    }

    @Override
    public int deleteHealthReportByIds(Long[] reportIds)
    {
        if (reportIds == null || reportIds.length == 0)
        {
            throw new ServiceException("请选择需要删除的报告");
        }
        return healthReportMapper.deleteHealthReportByIds(reportIds);
    }

    @Override
    public ReportExport exportDocx(Long reportId)
    {
        HealthReport report = healthReportMapper.selectHealthReportById(reportId);
        if (report == null)
        {
            throw new ServiceException("报告不存在或无权访问");
        }
        if (!"1".equals(report.getStatus()))
        {
            throw new ServiceException("报告未生成成功，无法导出");
        }
        ReportExport export = new ReportExport();
        export.setFileName("AI健康报告_" + report.getElderName() + "_" + report.getReportId() + ".docx");
        try
        {
            export.setContent(ReportDocxExporter.export(report));
        }
        catch (IOException e)
        {
            throw new ServiceException("报告文件生成失败：" + e.getMessage());
        }
        if (reportStorageProperties.isEnabled())
        {
            try
            {
                String url = reportFileStorage.store(export.getContent(), export.getFileName());
                export.setArchivedUrl(url);
                if (StringUtils.isNotEmpty(url))
                {
                    healthReportMapper.updateHealthReportArchive(report.getReportId(), url,
                            SecurityUtils.getUsername());
                }
            }
            catch (Exception e)
            {
                log.warn("报告归档失败，不影响下载：{}", e.getMessage());
            }
        }
        return export;
    }

    /**
     * 汇总老人档案、床位位置、评估记录与周期内业务统计
     */
    private ReportSourceData buildSourceData(Elder elder, Date start, Date end, String extraNote)
    {
        ReportSourceData data = new ReportSourceData();
        data.setElder(elder);
        data.setPeriodStart(start);
        data.setPeriodEnd(end);
        data.setExtraNote(extraNote);
        data.setLocation(healthReportMapper.selectBedLocationByElderId(elder.getElderId()));
        data.setCheckin(healthReportMapper.selectLatestCheckin(elder.getElderName(), elder.getIdCard()));
        data.setServiceTotal(healthReportMapper.countServiceOrders(elder.getElderId(), start, end));
        data.setServiceDone(healthReportMapper.countCompletedServiceOrders(elder.getElderId(), start, end));
        data.setServiceAmount(zeroIfNull(
                healthReportMapper.sumServiceAmount(elder.getElderId(), start, end)));
        data.setVisitCount(healthReportMapper.countVisits(elder.getElderId(), start, end));
        data.setUnpaidBillCount(healthReportMapper.countUnpaidBills(elder.getElderId()));
        data.setUnpaidBillAmount(zeroIfNull(healthReportMapper.sumUnpaidBills(elder.getElderId())));
        data.setPaidBillAmount(zeroIfNull(healthReportMapper.sumPaidBills(elder.getElderId())));

        // 新增：体征数据聚合（取最近 30 条，覆盖 30 天左右周期）
        data.setIndicators(buildHealthIndicators(elder.getElderId()));

        return data;
    }

    /**
     * 从最近 30 条 biz_health_record 记录里做聚合，得到 AI 报告可用的指标
     */
    private HealthIndicators buildHealthIndicators(Long elderId)
    {
        List<HealthRecord> records = healthRecordMapper.selectRecentByElderId(elderId, 30);
        HealthIndicators h = new HealthIndicators();
        if (records == null || records.isEmpty())
        {
            h.setRecordsCount(0);
            h.setOverallRiskHint("（最近 30 天未录入体征数据，建议先录入后再生成报告）");
            return h;
        }

        int n = records.size();
        h.setRecordsCount(n);

        // 先算 periodDays（首末两条的日期差）
        Date first = records.get(records.size() - 1).getRecordDate();
        Date last  = records.get(0).getRecordDate();
        if (first != null && last != null)
        {
            long days = ChronoUnit.DAYS.between(
                    first.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    last.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) + 1;
            h.setPeriodDays((int) Math.max(days, 1));
        }

        // 异常天数：distinct record_date 且存在 is_abnormal='1'
        java.util.Set<String> abnormalDates = new java.util.HashSet<>();
        for (HealthRecord r : records)
        {
            if ("1".equals(r.getIsAbnormal()) && r.getRecordDate() != null)
            {
                abnormalDates.add(new SimpleDateFormat("yyyy-MM-dd").format(r.getRecordDate()));
            }
        }
        h.setAbnormalDays(abnormalDates.size());

        /* 聚合：血压/心率/血糖/血氧 */
        long bpHighSum = 0, bpLowSum = 0, hrSum = 0;
        int bpHighN = 0, bpLowN = 0, hrN = 0;
        Integer bpHighMax = null, bpHighMin = null, bpLowMax = null, bpLowMin = null;
        Integer hrMax = null, hrMin = null;

        BigDecimal sugarSum = BigDecimal.ZERO;
        int sugarN = 0; BigDecimal sugarMax = null;

        BigDecimal oxygenSum = BigDecimal.ZERO;
        int oxygenN = 0; BigDecimal oxygenMin = null;

        for (HealthRecord r : records)
        {
            if (r.getBloodPressureHigh() != null)
            {
                bpHighSum += r.getBloodPressureHigh(); bpHighN++;
                if (bpHighMax == null || r.getBloodPressureHigh() > bpHighMax) bpHighMax = r.getBloodPressureHigh();
                if (bpHighMin == null || r.getBloodPressureHigh() < bpHighMin) bpHighMin = r.getBloodPressureHigh();
            }
            if (r.getBloodPressureLow() != null)
            {
                bpLowSum += r.getBloodPressureLow(); bpLowN++;
                if (bpLowMax == null || r.getBloodPressureLow() > bpLowMax) bpLowMax = r.getBloodPressureLow();
                if (bpLowMin == null || r.getBloodPressureLow() < bpLowMin) bpLowMin = r.getBloodPressureLow();
            }
            if (r.getHeartRate() != null)
            {
                hrSum += r.getHeartRate(); hrN++;
                if (hrMax == null || r.getHeartRate() > hrMax) hrMax = r.getHeartRate();
                if (hrMin == null || r.getHeartRate() < hrMin) hrMin = r.getHeartRate();
            }
            if (r.getBloodSugar() != null)
            {
                sugarSum = sugarSum.add(r.getBloodSugar()); sugarN++;
                if (sugarMax == null || r.getBloodSugar().compareTo(sugarMax) > 0) sugarMax = r.getBloodSugar();
            }
            if (r.getBloodOxygen() != null)
            {
                oxygenSum = oxygenSum.add(r.getBloodOxygen()); oxygenN++;
                if (oxygenMin == null || r.getBloodOxygen().compareTo(oxygenMin) < 0) oxygenMin = r.getBloodOxygen();
            }
        }
        if (bpHighN > 0) h.setBpHighAvg(BigDecimal.valueOf(bpHighSum).divide(BigDecimal.valueOf(bpHighN), 1, BigDecimal.ROUND_HALF_UP));
        h.setBpHighMax(bpHighMax); h.setBpHighMin(bpHighMin);
        if (bpLowN > 0) h.setBpLowAvg(BigDecimal.valueOf(bpLowSum).divide(BigDecimal.valueOf(bpLowN), 1, BigDecimal.ROUND_HALF_UP));
        h.setBpLowMax(bpLowMax); h.setBpLowMin(bpLowMin);
        if (hrN > 0)    h.setHrAvg(BigDecimal.valueOf(hrSum).divide(BigDecimal.valueOf(hrN), 1, BigDecimal.ROUND_HALF_UP));
        h.setHrMax(hrMax); h.setHrMin(hrMin);
        if (sugarN > 0) h.setSugarAvg(sugarSum.divide(BigDecimal.valueOf(sugarN), 2, BigDecimal.ROUND_HALF_UP));
        h.setSugarMax(sugarMax);
        if (oxygenN > 0) h.setOxygenAvg(oxygenSum.divide(BigDecimal.valueOf(oxygenN), 1, BigDecimal.ROUND_HALF_UP));
        h.setOxygenMin(oxygenMin);

        /* 最新一条（records 已经按日期倒序，index=0 就是最新） */
        HealthRecord latest = records.get(0);
        h.setLatestBpHigh(latest.getBloodPressureHigh());
        h.setLatestBpLow(latest.getBloodPressureLow());
        h.setLatestHr(latest.getHeartRate());
        h.setLatestBodyTemp(latest.getBodyTemp());
        h.setLatestSugar(latest.getBloodSugar());
        h.setLatestOxygen(latest.getBloodOxygen());
        if (latest.getRecordDate() != null)
        {
            h.setLatestDate(new SimpleDateFormat("yyyy-MM-dd").format(latest.getRecordDate()));
        }

        /* 综合风险提示（给 AI 一份先验判断，AI 可基于此展开） */
        StringBuilder risk = new StringBuilder();
        if (h.getAbnormalDays() > 0)
        {
            risk.append("最近 ").append(h.getAbnormalDays()).append(" 天出现过体征异常");
        }
        if (h.getBpHighMax() != null && h.getBpHighMax() > 160)
        {
            if (risk.length() > 0) risk.append("；");
            risk.append("收缩压峰值 ").append(h.getBpHighMax()).append(" mmHg（严重偏高）");
        }
        if (h.getHrMax() != null && h.getHrMax() > 110)
        {
            if (risk.length() > 0) risk.append("；");
            risk.append("心率峰值 ").append(h.getHrMax()).append("（心动过速风险）");
        }
        if (h.getSugarMax() != null && h.getSugarMax().compareTo(new BigDecimal("8.5")) > 0)
        {
            if (risk.length() > 0) risk.append("；");
            risk.append("血糖峰值 ").append(h.getSugarMax()).append(" mmol/L（显著偏高）");
        }
        if (h.getOxygenMin() != null && h.getOxygenMin().compareTo(new BigDecimal("92.0")) < 0)
        {
            if (risk.length() > 0) risk.append("；");
            risk.append("血氧最低 ").append(h.getOxygenMin()).append("%（需警惕低氧血症）");
        }
        if (risk.length() == 0)
        {
            risk.append("最近体征指标稳定，未发现显著异常峰值或异常天数。");
        }
        h.setOverallRiskHint(risk.toString());

        return h;
    }

    /**
     * 生成失败时保留失败记录，便于追踪与重试
     */
    private void saveFailedReport(HealthReport report, Exception e)
    {
        try
        {
            report.setStatus("2");
            report.setScore(0);
            report.setRiskLevel("2");
            report.setContent("");
            report.setSummary("报告生成失败");
            report.setErrorMsg(truncate(e.getMessage(), 500));
            report.setModelSource("qianfan".equalsIgnoreCase(aiReportProperties.getProvider()) ? "qianfan" : "mock");
            report.setModelName(aiReportProperties.getQianfan().getModel());
            healthReportMapper.insertHealthReport(report);
        }
        catch (Exception insertError)
        {
            log.warn("失败报告记录写入异常：{}", insertError.getMessage());
        }
    }

    private String normalizeReportType(String reportType)
    {
        if (StringUtils.isEmpty(reportType))
        {
            return "0";
        }
        if (!"0".equals(reportType) && !"1".equals(reportType) && !"2".equals(reportType))
        {
            throw new ServiceException("报告类型不正确");
        }
        return reportType;
    }

    private String summarize(String content)
    {
        String plain = content.replaceAll("[#*>`\\-|]", " ").replaceAll("\\s+", " ").trim();
        if (plain.length() <= SUMMARY_LENGTH)
        {
            return plain;
        }
        return plain.substring(0, SUMMARY_LENGTH) + "…";
    }

    private BigDecimal zeroIfNull(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Date today()
    {
        return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date addDays(Date date, int days)
    {
        return Date.from(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(days)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private long daysBetween(Date start, Date end)
    {
        return ChronoUnit.DAYS.between(start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) + 1;
    }

    private String truncate(String text, int maxLength)
    {
        if (text == null)
        {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}
