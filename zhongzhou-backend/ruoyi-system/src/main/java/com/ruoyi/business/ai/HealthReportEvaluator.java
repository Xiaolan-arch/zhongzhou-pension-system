package com.ruoyi.business.ai;

import com.ruoyi.business.domain.vo.ReportSourceData;

/**
 * 健康评分与风险等级规则
 *
 * @author ruoyi
 */
public class HealthReportEvaluator
{
    /** 评估结果：评分 + 风险等级 */
    public static class Evaluation
    {
        private final int score;

        private final String riskLevel;

        public Evaluation(int score, String riskLevel)
        {
            this.score = score;
            this.riskLevel = riskLevel;
        }

        public int getScore()
        {
            return score;
        }

        public String getRiskLevel()
        {
            return riskLevel;
        }
    }

    /**
     * 依据护理等级、服务完成率、探访频次、欠费情况计算评分与风险等级
     */
    public static Evaluation evaluate(ReportSourceData data)
    {
        int score = 100;
        String careLevel = data.getElder() == null ? null : data.getElder().getCareLevel();
        if ("2".equals(careLevel))
        {
            score -= 8;
        }
        else if ("3".equals(careLevel))
        {
            score -= 18;
        }
        else if ("4".equals(careLevel))
        {
            score -= 25;
        }

        if (data.getServiceTotal() > 0)
        {
            // 用整数差值计算，避免浮点误差导致 1.5 被舍入为 1
            score -= (int) Math.round((double) (data.getServiceTotal() - data.getServiceDone()) * 15
                    / data.getServiceTotal());
        }
        else
        {
            score -= 3;
        }

        if (data.getUnpaidBillCount() > 0)
        {
            score -= Math.min(12, data.getUnpaidBillCount() * 3);
        }

        if (data.getVisitCount() == 0)
        {
            score -= 5;
        }

        if (score < 40)
        {
            score = 40;
        }
        if (score > 100)
        {
            score = 100;
        }

        String riskLevel;
        if (score >= 80)
        {
            riskLevel = "0";
        }
        else if (score >= 65)
        {
            riskLevel = "1";
        }
        else
        {
            riskLevel = "2";
        }
        if ("4".equals(careLevel) && "0".equals(riskLevel))
        {
            riskLevel = "1";
        }
        return new Evaluation(score, riskLevel);
    }
}
