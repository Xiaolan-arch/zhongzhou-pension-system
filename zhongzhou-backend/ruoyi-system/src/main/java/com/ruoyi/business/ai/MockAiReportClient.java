package com.ruoyi.business.ai;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.ruoyi.business.domain.Checkin;
import com.ruoyi.business.domain.Elder;
import com.ruoyi.business.domain.vo.BedLocation;
import com.ruoyi.business.domain.vo.HealthIndicators;
import com.ruoyi.business.domain.vo.ReportSourceData;
import com.ruoyi.common.utils.StringUtils;

/**
 * 本地规则模拟的 AI 报告客户端，用于无千帆凭据时保证可演示
 *
 * @author ruoyi
 */
public class MockAiReportClient implements AiReportClient
{
    @Override
    public AiReportResult generate(ReportSourceData source, String systemPrompt, String userPrompt)
    {
        HealthReportEvaluator.Evaluation evaluation = HealthReportEvaluator.evaluate(source);
        String content = buildContent(source, evaluation);
        return new AiReportResult(content, "mock", "本地规则模版");
    }

    private String buildContent(ReportSourceData data, HealthReportEvaluator.Evaluation evaluation)
    {
        Elder elder = data.getElder();
        String careLevelName = HealthReportPromptBuilder.careLevelName(elder.getCareLevel());
        StringBuilder sb = new StringBuilder();

        sb.append("## 一、基本信息\n");
        sb.append("- 老人姓名：").append(elder.getElderName()).append("\n");
        sb.append("- 性别：").append("1".equals(elder.getGender()) ? "女" : "男").append("\n");
        sb.append("- 年龄：").append(elder.getAge() == null ? "未知" : elder.getAge()).append(" 岁\n");
        sb.append("- 护理等级：").append(careLevelName).append("\n");
        sb.append("- 统计周期：").append(formatDate(data.getPeriodStart())).append(" 至 ")
                .append(formatDate(data.getPeriodEnd())).append("\n");
        BedLocation location = data.getLocation();
        if (location != null && StringUtils.isNotEmpty(location.getRoomNo()))
        {
            sb.append("- 当前床位：").append(location.getFloorNo()).append("层 ")
                    .append(location.getRoomNo()).append("房 ").append(location.getBedNo()).append("床\n");
        }
        sb.append("\n");

        sb.append("## 二、健康评估摘要\n");
        sb.append("综合护理等级、照护服务完成率、家属探访与费用结算情况，本周期健康评分为 **")
                .append(evaluation.getScore()).append(" 分**，风险等级为 **")
                .append(riskName(evaluation.getRiskLevel())).append("**。");
        Checkin checkin = data.getCheckin();
        if (checkin != null && StringUtils.isNotEmpty(checkin.getAssessmentResult()))
        {
            sb.append("入住评估结论显示：").append(checkin.getAssessmentResult()).append("。");
        }
        if (checkin != null && StringUtils.isNotEmpty(checkin.getDisease()))
        {
            sb.append("既往疾病情况：").append(checkin.getDisease()).append("。");
        }
        sb.append("\n\n");

        sb.append("## 三、照护服务情况\n");
        sb.append("- 本周期共执行服务订单 ").append(data.getServiceTotal()).append(" 次，已完成 ")
                .append(data.getServiceDone()).append(" 次");
        if (data.getServiceTotal() > 0)
        {
            sb.append("，完成率 ").append(percent(data.getServiceDone(), data.getServiceTotal())).append("%");
        }
        sb.append("。\n");
        sb.append("- 服务费用合计 ").append(money(data.getServiceAmount())).append(" 元。\n");
        sb.append("- 家属探访 ").append(data.getVisitCount()).append(" 次。\n\n");

        // --- 新增：体征数据趋势分析（P8 升级） ---
        HealthIndicators ind = data.getIndicators();
        sb.append("## 四、体征数据趋势分析（AI 基于真实体征记录生成）\n");
        if (ind == null || ind.getRecordsCount() == 0)
        {
            sb.append("- （最近 30 天未录入体征数据，建议先录入体征记录后再生成报告）\n\n");
        }
        else
        {
            sb.append("- 统计范围：最近 ").append(ind.getRecordsCount()).append(" 条记录 / ")
                    .append(ind.getPeriodDays() > 0 ? ind.getPeriodDays() + " 天" : "-").append("\n");
            sb.append("- 异常天数：").append(ind.getAbnormalDays()).append(" 天\n");
            sb.append("- 综合风险提示：").append(ind.getOverallRiskHint()).append("\n\n");

            sb.append("**血压（收缩压 / 舒张压）**\n");
            sb.append("- 收缩压 均值 ").append(ind.getBpHighAvg() == null ? "-" : ind.getBpHighAvg())
                    .append("  mmHg，范围 ")
                    .append(intRange(ind.getBpHighMin(), ind.getBpHighMax())).append("\n");
            sb.append("- 舒张压 均值 ").append(ind.getBpLowAvg() == null ? "-" : ind.getBpLowAvg())
                    .append("  mmHg，范围 ")
                    .append(intRange(ind.getBpLowMin(), ind.getBpLowMax())).append("\n\n");

            sb.append("**心率 / 血糖 / 血氧**\n");
            sb.append("- 心率 均值 ").append(ind.getHrAvg() == null ? "-" : ind.getHrAvg())
                    .append("，范围 ").append(intRange(ind.getHrMin(), ind.getHrMax())).append("\n");
            sb.append("- 血糖 均值 ").append(ind.getSugarAvg() == null ? "-" : ind.getSugarAvg())
                    .append(" mmol/L，峰值 ").append(ind.getSugarMax() == null ? "-" : ind.getSugarMax()).append("\n");
            sb.append("- 血氧 均值 ").append(ind.getOxygenAvg() == null ? "-" : ind.getOxygenAvg())
                    .append("%，最低 ").append(ind.getOxygenMin() == null ? "-" : ind.getOxygenMin()).append("\n\n");

            sb.append("**最近一次体征**（").append(StringUtils.isEmpty(ind.getLatestDate()) ? "-" : ind.getLatestDate()).append("）\n");
            sb.append("- 收缩压 / 舒张压 / 心率：")
                    .append(intOrDash(ind.getLatestBpHigh())).append(" / ")
                    .append(intOrDash(ind.getLatestBpLow())).append(" / ")
                    .append(intOrDash(ind.getLatestHr())).append("\n");
            sb.append("- 血糖 / 血氧：")
                    .append(decOrDash(ind.getLatestSugar())).append(" / ")
                    .append(decOrDash(ind.getLatestOxygen())).append("\n\n");

            // 在风险提示里也加体征相关的风险
            if (ind.getAbnormalDays() > 0)
            {
                // 风险提示部分后面追加
            }
        }

        sb.append("## 五、费用与账单\n");
        sb.append("- 未结清账单 ").append(data.getUnpaidBillCount()).append(" 笔，金额 ")
                .append(money(data.getUnpaidBillAmount())).append(" 元。\n");
        sb.append("- 已缴费金额 ").append(money(data.getPaidBillAmount())).append(" 元。\n");
        if (data.getUnpaidBillCount() > 0)
        {
            sb.append("- 建议尽快完成缴费，避免影响后续服务排期。\n");
        }
        sb.append("\n");

        sb.append("## 六、风险提示\n");
        boolean hasRisk = false;
        if ("3".equals(elder.getCareLevel()) || "4".equals(elder.getCareLevel()))
        {
            sb.append("- 老人属于").append(careLevelName).append("，跌倒、压疮、误吸等风险较高，需重点防范。\n");
            hasRisk = true;
        }
        if (data.getUnpaidBillCount() > 0)
        {
            sb.append("- 存在未结清账单，需关注费用结算。\n");
            hasRisk = true;
        }
        if (data.getVisitCount() == 0)
        {
            sb.append("- 本周期无家属探访记录，需关注老人情绪与社交状态。\n");
            hasRisk = true;
        }
        if (ind != null && ind.getAbnormalDays() > 0)
        {
            sb.append("- 最近 ").append(ind.getAbnormalDays()).append(" 天出现体征异常，").append(ind.getOverallRiskHint()).append("。\n");
            hasRisk = true;
        }
        if (!hasRisk)
        {
            sb.append("- 本周期未发现明显异常，继续保持现有照护节奏即可。\n");
        }
        sb.append("\n");

        sb.append("## 七、照护建议\n");
        if ("1".equals(elder.getCareLevel()))
        {
            sb.append("1. 维持现有照护方案，鼓励老人参与院内文娱与康复活动。\n");
            sb.append("2. 每周测量一次血压、体重并记录变化。\n");
        }
        else if ("2".equals(elder.getCareLevel()))
        {
            sb.append("1. 维持半自理照护方案，重点关注行走与如厕安全。\n");
            sb.append("2. 每周安排 2-3 次下肢康复训练，延缓肌力下降。\n");
        }
        else if ("3".equals(elder.getCareLevel()))
        {
            sb.append("1. 每 2 小时协助翻身一次，检查皮肤受压情况，预防压疮。\n");
            sb.append("2. 进食时保持半卧位，观察吞咽情况，预防误吸。\n");
        }
        else
        {
            sb.append("1. 落实特护巡视制度，密切观察生命体征与意识状态。\n");
            sb.append("2. 做好管路、皮肤与口腔护理，异常情况及时上报。\n");
        }
        sb.append("3. 保持居室地面干燥、夜间开启感应照明，降低跌倒风险。\n");
        sb.append("4. 根据服务完成率持续优化护理排班，确保订单按时执行。\n\n");

        sb.append("## 八、家属沟通建议\n");
        sb.append("- 建议家属每周至少探访或电话沟通一次，关注老人情绪、饮食与睡眠变化。\n");
        sb.append("- 如老人出现食欲下降、精神萎靡或用药异常，请及时与护理站联系。\n");
        sb.append("- 账单缴费与合同续签事宜，请家属配合机构完成。\n");
        return sb.toString();
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

    private static int percent(int part, int total)
    {
        return total <= 0 ? 0 : (int) Math.round(part * 100.0 / total);
    }

    private static String money(BigDecimal amount)
    {
        return amount == null ? "0.00" : amount.toPlainString();
    }

    private static String formatDate(Date date)
    {
        return date == null ? "-" : new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private static String intRange(Integer min, Integer max)
    {
        if (min == null && max == null) return "-";
        if (min == null) return max + "~";
        if (max == null) return "~" + min;
        return min + "~" + max;
    }

    private static String intOrDash(Integer v)
    {
        return v == null ? "-" : v.toString();
    }

    private static String decOrDash(BigDecimal v)
    {
        return v == null ? "-" : v.toPlainString();
    }
}
