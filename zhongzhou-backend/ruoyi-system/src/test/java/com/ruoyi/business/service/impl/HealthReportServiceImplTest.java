package com.ruoyi.business.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.ruoyi.business.ai.AiReportClient;
import com.ruoyi.business.ai.AiReportProperties;
import com.ruoyi.business.ai.AiReportResult;
import com.ruoyi.business.domain.Elder;
import com.ruoyi.business.domain.HealthReport;
import com.ruoyi.business.domain.vo.ReportExport;
import com.ruoyi.business.domain.vo.ReportSourceData;
import com.ruoyi.business.mapper.ElderMapper;
import com.ruoyi.business.mapper.HealthReportMapper;
import com.ruoyi.business.storage.IReportFileStorage;
import com.ruoyi.business.storage.ReportStorageProperties;
import com.ruoyi.common.exception.ServiceException;

/**
 * AI 健康报告服务：参数校验、生成落库、失败留痕、归档与删除
 */
public class HealthReportServiceImplTest extends BusinessTestSupport
{
    private HealthReportServiceImpl service;

    private AiReportProperties aiProperties;

    private ReportStorageProperties storageProperties;

    @Before
    public void setUp()
    {
        loginAs(2L, 1L, "admin");
        service = new HealthReportServiceImpl();
        aiProperties = new AiReportProperties();
        storageProperties = new ReportStorageProperties();
        storageProperties.setEnabled(false);
        inject(service, "aiReportProperties", aiProperties);
        inject(service, "reportStorageProperties", storageProperties);
    }

    @After
    public void tearDown()
    {
        logout();
    }

    private void bind(StubHandler reportHandler, StubHandler elderHandler, AiReportClient client)
    {
        inject(service, "healthReportMapper", stub(HealthReportMapper.class, reportHandler));
        inject(service, "elderMapper", stub(ElderMapper.class, elderHandler));
        inject(service, "aiReportClient", client);
        inject(service, "reportFileStorage", new IReportFileStorage() {
            @Override
            public String store(byte[] content, String fileName) throws IOException
            {
                return "/profile/report/" + fileName;
            }
        });
    }

    private Elder inResidenceElder()
    {
        Elder elder = new Elder();
        elder.setElderId(1L);
        elder.setElderName("张伟");
        elder.setGender("0");
        elder.setAge(78);
        elder.setCareLevel("2");
        elder.setStatus("0");
        elder.setIdCard("");
        return elder;
    }

    private AiReportClient successClient(final String content)
    {
        return new AiReportClient() {
            @Override
            public AiReportResult generate(ReportSourceData source, String systemPrompt, String userPrompt)
            {
                return new AiReportResult(content, "mock", "本地规则模版");
            }
        };
    }

    @Test
    public void generateRejectsMissingElder()
    {
        bind(new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return NOT_STUBBED;
            }
        }, new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return NOT_STUBBED;
            }
        }, successClient("content"));
        try
        {
            service.generateReport(999L, "0", null, null, null);
            fail("老人不存在时应当抛出异常");
        }
        catch (ServiceException e)
        {
            assertEquals("老人档案不存在或无权访问", e.getMessage());
        }
    }

    @Test
    public void generateRejectsRetiredElder()
    {
        final Elder retired = inResidenceElder();
        retired.setStatus("1");
        bind(new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return NOT_STUBBED;
            }
        }, new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return "selectElderById".equals(methodName) ? retired : NOT_STUBBED;
            }
        }, successClient("content"));
        try
        {
            service.generateReport(1L, "0", null, null, null);
            fail("已退住老人不应生成报告");
        }
        catch (ServiceException e)
        {
            assertEquals("仅可为在住老人生成健康报告", e.getMessage());
        }
    }

    @Test
    public void generateRejectsReversedPeriod()
    {
        final Elder elder = inResidenceElder();
        bind(new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return NOT_STUBBED;
            }
        }, new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return "selectElderById".equals(methodName) ? elder : NOT_STUBBED;
            }
        }, successClient("content"));
        Date end = new Date();
        Date start = new Date(end.getTime() + 86400000L);
        try
        {
            service.generateReport(1L, "0", start, end, null);
            fail("周期开始晚于结束时应抛出异常");
        }
        catch (ServiceException e)
        {
            assertEquals("统计周期开始日期不能晚于结束日期", e.getMessage());
        }
    }

    @Test
    public void generateRejectsInvalidType()
    {
        final Elder elder = inResidenceElder();
        bind(new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return NOT_STUBBED;
            }
        }, new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return "selectElderById".equals(methodName) ? elder : NOT_STUBBED;
            }
        }, successClient("content"));
        try
        {
            service.generateReport(1L, "9", null, null, null);
            fail("非法报告类型应抛出异常");
        }
        catch (ServiceException e)
        {
            assertEquals("报告类型不正确", e.getMessage());
        }
    }

    @Test
    public void generateSuccessSavesReport()
    {
        final Elder elder = inResidenceElder();
        final HealthReport[] inserted = new HealthReport[1];
        StubHandler reportHandler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                if ("countServiceOrders".equals(methodName))
                {
                    return 10;
                }
                if ("countCompletedServiceOrders".equals(methodName))
                {
                    return 9;
                }
                if ("countVisits".equals(methodName))
                {
                    return 2;
                }
                if ("sumServiceAmount".equals(methodName))
                {
                    return new BigDecimal("1200.00");
                }
                if ("sumUnpaidBills".equals(methodName))
                {
                    return BigDecimal.ZERO;
                }
                if ("sumPaidBills".equals(methodName))
                {
                    return new BigDecimal("3600.00");
                }
                if ("insertHealthReport".equals(methodName))
                {
                    HealthReport report = (HealthReport) args[0];
                    report.setReportId(99L);
                    inserted[0] = report;
                    return 1;
                }
                return NOT_STUBBED;
            }
        };
        StubHandler elderHandler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return "selectElderById".equals(methodName) ? elder : NOT_STUBBED;
            }
        };
        bind(reportHandler, elderHandler, successClient("## 一、基本信息\n- 老人姓名：张伟\n\n## 二、健康评估摘要\n状态平稳。"));

        HealthReport report = service.generateReport(1L, "0", null, null, "关注睡眠");

        assertEquals(Long.valueOf(99L), report.getReportId());
        assertNotNull("报告应已写入", inserted[0]);
        assertEquals("成功状态", "1", inserted[0].getStatus());
        assertEquals(Long.valueOf(1L), inserted[0].getTenantId());
        assertEquals("mock", inserted[0].getModelSource());
        assertNotNull("摘要不应为空", inserted[0].getSummary());
        assertFalse("摘要不应包含 Markdown 标题符号", inserted[0].getSummary().contains("#"));
        assertEquals("护理等级半自理、完成率90%、探访2次 => 90 分", Integer.valueOf(90), inserted[0].getScore());
        assertEquals("90 分应为良好", "0", inserted[0].getRiskLevel());
        assertTrue("生成内容应写入正文", inserted[0].getContent().contains("张伟"));
    }

    @Test
    public void generateFailureSavesFailedRecord()
    {
        final Elder elder = inResidenceElder();
        final HealthReport[] failed = new HealthReport[1];
        StubHandler reportHandler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                if ("insertHealthReport".equals(methodName))
                {
                    HealthReport report = (HealthReport) args[0];
                    failed[0] = report;
                    return 1;
                }
                return NOT_STUBBED;
            }
        };
        StubHandler elderHandler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return "selectElderById".equals(methodName) ? elder : NOT_STUBBED;
            }
        };
        bind(reportHandler, elderHandler, new AiReportClient() {
            @Override
            public AiReportResult generate(ReportSourceData source, String systemPrompt, String userPrompt)
            {
                throw new ServiceException("千帆接口调用失败：timeout");
            }
        });
        try
        {
            service.generateReport(1L, "1", null, null, null);
            fail("模型调用失败时应抛出异常");
        }
        catch (ServiceException e)
        {
            assertEquals("千帆接口调用失败：timeout", e.getMessage());
        }
        assertNotNull("失败记录应留痕", failed[0]);
        assertEquals("失败状态", "2", failed[0].getStatus());
        assertTrue("失败原因应记录", failed[0].getErrorMsg().contains("timeout"));
    }

    @Test
    public void deleteRejectsEmptyIds()
    {
        bind(new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return NOT_STUBBED;
            }
        }, new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return NOT_STUBBED;
            }
        }, successClient("content"));
        try
        {
            service.deleteHealthReportByIds(new Long[0]);
            fail("空ID数组应抛出异常");
        }
        catch (ServiceException e)
        {
            assertEquals("请选择需要删除的报告", e.getMessage());
        }
    }

    @Test
    public void regenerateUsesHistoryParameters()
    {
        final Elder elder = inResidenceElder();
        final HealthReport history = new HealthReport();
        history.setReportId(5L);
        history.setElderId(1L);
        history.setReportType("1");
        history.setPeriodStart(new Date());
        history.setPeriodEnd(new Date());
        history.setRemark("历史补充说明");
        final HealthReport[] inserted = new HealthReport[1];
        StubHandler reportHandler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                if ("selectHealthReportById".equals(methodName))
                {
                    return history;
                }
                if ("insertHealthReport".equals(methodName))
                {
                    inserted[0] = (HealthReport) args[0];
                    return 1;
                }
                return NOT_STUBBED;
            }
        };
        StubHandler elderHandler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return "selectElderById".equals(methodName) ? elder : NOT_STUBBED;
            }
        };
        bind(reportHandler, elderHandler, successClient("## 健康评估\n状态平稳"));

        service.regenerateReport(5L);

        assertNotNull(inserted[0]);
        assertEquals("报告类型应沿用历史记录", "1", inserted[0].getReportType());
        assertEquals("补充说明应沿用历史记录", "历史补充说明", inserted[0].getRemark());
    }

    @Test
    public void exportArchivesWordFile()
    {
        final HealthReport report = new HealthReport();
        report.setReportId(7L);
        report.setElderName("张伟");
        report.setReportType("0");
        report.setStatus("1");
        report.setRiskLevel("0");
        report.setScore(90);
        report.setContent("## 一、基本信息\n- 老人姓名：张伟");
        storageProperties.setEnabled(true);
        final String[] archived = new String[1];
        StubHandler reportHandler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                if ("selectHealthReportById".equals(methodName))
                {
                    return report;
                }
                if ("updateHealthReportArchive".equals(methodName))
                {
                    archived[0] = (String) args[1];
                    return 1;
                }
                return NOT_STUBBED;
            }
        };
        bind(reportHandler, new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return NOT_STUBBED;
            }
        }, successClient("content"));

        ReportExport export = service.exportDocx(7L);

        assertNotNull("应生成 Word 内容", export.getContent());
        assertTrue("文件名应以 .docx 结尾", export.getFileName().endsWith(".docx"));
        assertEquals("/profile/report/AI健康报告_张伟_7.docx", export.getArchivedUrl());
        assertEquals("归档地址应回写报告", export.getArchivedUrl(), archived[0]);
    }
}
