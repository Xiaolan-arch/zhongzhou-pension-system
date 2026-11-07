package com.ruoyi.business.ai;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.ruoyi.business.domain.Checkin;
import com.ruoyi.business.domain.Elder;
import com.ruoyi.business.domain.vo.BedLocation;
import com.ruoyi.business.domain.vo.HealthIndicators;
import com.ruoyi.business.domain.vo.ReportSourceData;
import com.ruoyi.common.utils.StringUtils;

/**
 * 报告提示词构建器
 *
 * @author ruoyi
 */
public class HealthReportPromptBuilder
{
    /** 报告类型名称 */
    public static String reportTypeName(String reportType)
    {
        if ("1".equals(reportType))
        {
            return "健康评估报告";
        }
        if ("2".equals(reportType))
        {
            return "月度总结报告";
        }
        return "综合照护报告";
    }

    /** 护理等级名称 */
    public static String careLevelName(String careLevel)
    {
        if ("1".equals(careLevel))
        {
            return "完全自理";
        }
        if ("2".equals(careLevel))
        {
            return "半自理";
        }
        if ("3".equals(careLevel))
        {
            return "重度失能";
        }
        if ("4".equals(careLevel))
        {
            return "特护";
        }
        return "未评估";
    }

    /** 系统提示词 */
    public static String buildSystemPrompt()
    {
        return "你是养老机构的资深护理评估师，熟悉老年人照护分级、常见慢性病管理与风险防范。"
                + "请基于机构提供的真实数据撰写中文健康报告，语言专业、温暖、客观，禁止编造数据或做医疗诊断。"
                + "输出使用 Markdown 格式，至少包含：基本信息、健康评估摘要、照护服务情况、费用与账单、风险提示、照护建议、家属沟通建议。";
    }

    /** 用户提示词 */
    public static String buildUserPrompt(ReportSourceData data, String reportType)
    {
        Elder elder = data.getElder();
        StringBuilder sb = new StringBuilder();
        sb.append("请生成一份【").append(reportTypeName(reportType)).append("】。\n");
        sb.append("统计周期：").append(formatDate(data.getPeriodStart())).append(" 至 ")
                .append(formatDate(data.getPeriodEnd())).append("\n\n");

        sb.append("## 老人基本信息\n");
        sb.append("- 姓名：").append(elder.getElderName()).append("\n");
        sb.append("- 性别：").append("1".equals(elder.getGender()) ? "女" : "男").append("\n");
        sb.append("- 年龄：").append(elder.getAge() == null ? "未知" : elder.getAge()).append("\n");
        sb.append("- 护理等级：").append(careLevelName(elder.getCareLevel())).append("\n");
        sb.append("- 入住日期：").append(formatDate(elder.getCheckinDate())).append("\n");
        BedLocation location = data.getLocation();
        if (location != null && StringUtils.isNotEmpty(location.getRoomNo()))
        {
            sb.append("- 当前床位：").append(location.getFloorNo()).append("层 ")
                    .append(location.getRoomNo()).append("房 ").append(location.getBedNo()).append("床\n");
        }
        sb.append("\n");

        Checkin checkin = data.getCheckin();
        if (checkin != null)
        {
            sb.append("## 入住评估记录\n");
            sb.append("- 自理能力：").append(nullToDash(checkin.getSelfCare())).append("\n");
            sb.append("- 精神状态：").append(nullToDash(checkin.getMentalState())).append("\n");
            sb.append("- 疾病情况：").append(nullToDash(checkin.getDisease())).append("\n");
            sb.append("- 评估结论：").append(nullToDash(checkin.getAssessmentResult())).append("\n\n");
        }

        // --- 体征数据（AI 报告升级：从 biz_health_record 聚合的真实数据） ---
        HealthIndicators ind = data.getIndicators();
        if (ind != null)
        {
            sb.append("## 体征数据趋势分析（最近 ").append(ind.getRecordsCount()).append(" 条 / ")
                    .append(ind.getPeriodDays() > 0 ? ind.getPeriodDays() + " 天" : "-").append("）\n");
            sb.append("- 异常天数：").append(ind.getAbnormalDays()).append(" 天\n");
            sb.append("- 综合风险提示：").append(ind.getOverallRiskHint()).append("\n\n");

            sb.append("**血压（收缩压 / 舒张压）**\n");
            sb.append("- 收缩压 均值：").append(ind.getBpHighAvg() == null ? "-" : ind.getBpHighAvg())
                    .append("  范围：").append(nullToDash(intRange(ind.getBpHighMin(), ind.getBpHighMax())))
                    .append("\n");
            sb.append("- 舒张压 均值：").append(ind.getBpLowAvg() == null ? "-" : ind.getBpLowAvg())
                    .append("  范围：").append(nullToDash(intRange(ind.getBpLowMin(), ind.getBpLowMax())))
                    .append("\n\n");

            sb.append("**心率 / 体温**\n");
            sb.append("- 心率 均值：").append(ind.getHrAvg() == null ? "-" : ind.getHrAvg())
                    .append("  范围：").append(nullToDash(intRange(ind.getHrMin(), ind.getHrMax())))
                    .append("\n");
            sb.append("- 最新体温：").append(ind.getLatestBodyTemp() == null ? "-" : ind.getLatestBodyTemp()).append(" ℃\n\n");

            sb.append("**血糖 / 血氧**\n");
            sb.append("- 血糖 均值：").append(ind.getSugarAvg() == null ? "-" : ind.getSugarAvg())
                    .append("  峰值：").append(ind.getSugarMax() == null ? "-" : ind.getSugarMax()).append("\n");
            sb.append("- 血氧 均值：").append(ind.getOxygenAvg() == null ? "-" : ind.getOxygenAvg())
                    .append("  最低：").append(ind.getOxygenMin() == null ? "-" : ind.getOxygenMin()).append("\n\n");

            sb.append("**最近一次体征**（").append(nullToDash(ind.getLatestDate())).append("）\n");
            sb.append("- 收缩压 / 舒张压 / 心率：")
                    .append(nullToDash(ind.getLatestBpHigh())).append(" / ")
                    .append(nullToDash(ind.getLatestBpLow())).append(" / ")
                    .append(nullToDash(ind.getLatestHr())).append("\n");
            sb.append("- 血糖 / 血氧：")
                    .append(nullToDash(ind.getLatestSugar())).append(" / ")
                    .append(nullToDash(ind.getLatestOxygen())).append("\n\n");
        }

        sb.append("## 周期内照护数据\n");
        sb.append("- 服务订单总数：").append(data.getServiceTotal()).append("，已完成：").append(data.getServiceDone())
                .append("\n");
        sb.append("- 服务费用合计：").append(data.getServiceAmount() == null ? "0.00" : data.getServiceAmount())
                .append(" 元\n");
        sb.append("- 家属探访次数：").append(data.getVisitCount()).append("\n");
        sb.append("- 未结清账单：").append(data.getUnpaidBillCount()).append(" 笔，未结清金额：")
                .append(data.getUnpaidBillAmount() == null ? "0.00" : data.getUnpaidBillAmount()).append(" 元\n");
        sb.append("- 已缴费金额：").append(data.getPaidBillAmount() == null ? "0.00" : data.getPaidBillAmount())
                .append(" 元\n\n");

        if (StringUtils.isNotEmpty(data.getExtraNote()))
        {
            sb.append("## 补充说明\n").append(data.getExtraNote()).append("\n\n");
        }
        sb.append("请输出 500-900 字报告正文，不要重复原始数据表格，不要输出诊断结论。");
        return sb.toString();
    }

    private static String formatDate(Date date)
    {
        return date == null ? "-" : new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private static String nullToDash(String value)
    {
        return StringUtils.isEmpty(value) ? "-" : value;
    }

    private static String nullToDash(Object value)
    {
        return value == null ? "-" : value.toString();
    }

    /** 整数范围格式化：min~max，任一项缺失返回 "-" */
    private static String intRange(Integer min, Integer max)
    {
        if (min == null && max == null) return "-";
        if (min == null) return max + "~";
        if (max == null) return "~" + min;
        return min + "~" + max;
    }
}
