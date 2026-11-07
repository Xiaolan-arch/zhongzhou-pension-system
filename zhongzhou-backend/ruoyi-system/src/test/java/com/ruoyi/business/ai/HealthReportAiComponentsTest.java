package com.ruoyi.business.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.math.BigDecimal;
import java.util.Date;
import org.junit.Test;
import com.ruoyi.business.domain.Elder;
import com.ruoyi.business.domain.vo.ReportSourceData;

/**
 * AI 报告组件：评分规则、提示词与本地模拟内容
 */
public class HealthReportAiComponentsTest
{
    private ReportSourceData buildData(String careLevel, int total, int done, int visits, int unpaid)
    {
        Elder elder = new Elder();
        elder.setElderId(1L);
        elder.setElderName("张伟");
        elder.setGender("0");
        elder.setAge(78);
        elder.setCareLevel(careLevel);
        elder.setStatus("0");
        elder.setIdCard("110101195001019999");

        ReportSourceData data = new ReportSourceData();
        data.setElder(elder);
        data.setPeriodStart(new Date());
        data.setPeriodEnd(new Date());
        data.setServiceTotal(total);
        data.setServiceDone(done);
        data.setServiceAmount(new BigDecimal("1200.00"));
        data.setVisitCount(visits);
        data.setUnpaidBillCount(unpaid);
        data.setUnpaidBillAmount(new BigDecimal("2600.00"));
        data.setPaidBillAmount(new BigDecimal("3600.00"));
        return data;
    }

    @Test
    public void selfCareElderGetsFullScore()
    {
        HealthReportEvaluator.Evaluation evaluation = HealthReportEvaluator.evaluate(
                buildData("1", 10, 10, 2, 0));
        assertEquals(100, evaluation.getScore());
        assertEquals("0", evaluation.getRiskLevel());
    }

    @Test
    public void deductionRulesProduceAttentionRisk()
    {
        HealthReportEvaluator.Evaluation evaluation = HealthReportEvaluator.evaluate(
                buildData("2", 10, 4, 0, 1));
        assertEquals("半自理 -8、完成率60% -9、无探访 -5、欠费1笔 -3", 75, evaluation.getScore());
        assertEquals("1", evaluation.getRiskLevel());
    }

    @Test
    public void severeCareElderFallsIntoWarning()
    {
        HealthReportEvaluator.Evaluation evaluation = HealthReportEvaluator.evaluate(
                buildData("4", 10, 0, 0, 3));
        assertEquals("特护 -25、完成率0% -15、无探访 -5、欠费3笔 -9", 46, evaluation.getScore());
        assertEquals("2", evaluation.getRiskLevel());
    }

    @Test
    public void scoreNeverBelowForty()
    {
        HealthReportEvaluator.Evaluation evaluation = HealthReportEvaluator.evaluate(
                buildData("4", 0, 0, 0, 6));
        assertTrue("评分下限应为 40", evaluation.getScore() >= 40);
        assertEquals("2", evaluation.getRiskLevel());
    }

    @Test
    public void promptContainsElderAndPeriod()
    {
        ReportSourceData data = buildData("2", 10, 8, 3, 0);
        data.setExtraNote("关注睡眠");
        String prompt = HealthReportPromptBuilder.buildUserPrompt(data, "0");
        assertTrue(prompt.contains("张伟"));
        assertTrue(prompt.contains("半自理"));
        assertTrue(prompt.contains("关注睡眠"));
        assertTrue(prompt.contains("综合照护报告"));
        assertEquals("半自理", HealthReportPromptBuilder.careLevelName("2"));
    }

    @Test
    public void mockClientProducesStructuredReport()
    {
        MockAiReportClient client = new MockAiReportClient();
        ReportSourceData data = buildData("2", 10, 9, 3, 0);
        AiReportResult result = client.generate(data, HealthReportPromptBuilder.buildSystemPrompt(),
                HealthReportPromptBuilder.buildUserPrompt(data, "0"));
        assertEquals("mock", result.getModelSource());
        assertEquals("本地规则模版", result.getModelName());
        assertTrue(result.getContent().contains("## 一、基本信息"));
        assertTrue(result.getContent().contains("张伟"));
        assertTrue(result.getContent().contains("## 六、照护建议"));
        assertTrue(result.getContent().contains("良好"));
    }
}
