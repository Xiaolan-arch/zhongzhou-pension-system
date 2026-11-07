package com.ruoyi.business.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.ruoyi.business.mapper.DashboardMapper;
import com.ruoyi.system.domain.SysTenant;
import com.ruoyi.system.service.ISysTenantService;

/**
 * 工作台统计服务：指标计算、月份补全、分布文案、待办与租户视角
 */
public class DashboardServiceImplTest extends BusinessTestSupport
{
    private DashboardServiceImpl service;

    private Map<String, Object> numbers = new HashMap<String, Object>();

    @Before
    public void setUp()
    {
        loginAs(2L, 1L, "admin");
        service = new DashboardServiceImpl();
        numbers = new HashMap<String, Object>();
        inject(service, "dashboardMapper", mapper());
        inject(service, "sysTenantService", tenantService());
    }

    @After
    public void tearDown()
    {
        logout();
    }

    private DashboardMapper mapper()
    {
        return stub(DashboardMapper.class, new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                if (numbers.containsKey(methodName))
                {
                    return numbers.get(methodName);
                }
                if (methodName.startsWith("select"))
                {
                    return new ArrayList<Map<String, Object>>();
                }
                return NOT_STUBBED;
            }
        });
    }

    private ISysTenantService tenantService()
    {
        return stub(ISysTenantService.class, new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                if ("selectTenantById".equals(methodName))
                {
                    SysTenant tenant = new SysTenant();
                    tenant.setTenantId((Long) args[0]);
                    tenant.setTenantName("中州养老院");
                    return tenant;
                }
                return NOT_STUBBED;
            }
        });
    }

    private void put(String key, Object value)
    {
        numbers.put(key, value);
    }

    private String currentMonth()
    {
        return new java.text.SimpleDateFormat("yyyy-MM").format(new java.util.Date());
    }

    @Test
    public void summaryComputesOccupancyAndIncome()
    {
        put("countAllElders", 28L);
        put("countInResidenceElders", 26L);
        put("countPendingElders", 2L);
        put("countBeds", 32L);
        put("countOccupiedBeds", 28L);
        put("countRooms", 16L);
        put("sumMonthReceivable", new BigDecimal("42000.00"));
        put("sumMonthReceived", new BigDecimal("38000.00"));
        put("sumUnpaidAmount", new BigDecimal("4000.00"));
        put("countUnpaidBills", 6L);
        put("countTodayPendingVisits", 2L);
        put("countTodayVisits", 5L);
        put("countPendingServiceOrders", 4L);
        put("countUnfinishedServiceOrders", 7L);
        put("countHighRiskReports", 1L);

        Map<String, Object> summary = service.getSummary();

        assertEquals("机构视角", "tenant", summary.get("scope"));
        assertEquals("机构名称取自租户表", "中州养老院", summary.get("tenantName"));
        assertEquals("在住人数", 26L, summary.get("inResidence"));
        assertEquals("空余床位", 4L, summary.get("bedFree"));
        assertEquals("床位使用率 87.5%", 87.5, summary.get("bedOccupancy"));
        assertEquals("收入完成率四舍五入到 1 位", 90.5, summary.get("monthIncomeRate"));
        assertEquals("待缴金额", new BigDecimal("4000.00"), summary.get("unpaidTotal"));
        assertEquals("未完成服务", 7L, summary.get("serviceUnfinished"));
    }

    @Test
    public void summaryHandlesZeroBedsAndZeroReceivable()
    {
        put("countBeds", 0L);
        put("countOccupiedBeds", 0L);
        put("sumMonthReceivable", BigDecimal.ZERO);
        put("sumMonthReceived", BigDecimal.ZERO);

        Map<String, Object> summary = service.getSummary();

        assertEquals("无床位时使用率为 0", 0.0, summary.get("bedOccupancy"));
        assertEquals("无应收时完成率为 0", 0.0, summary.get("monthIncomeRate"));
        assertEquals("空值金额兜底为 0", BigDecimal.ZERO, summary.get("unpaidTotal"));
    }

    @Test
    public void trendFillsMissingMonths()
    {
        List<Map<String, Object>> checkin = new ArrayList<Map<String, Object>>();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("month", currentMonth());
        row.put("checkin", 3L);
        checkin.add(row);
        put("selectCheckinTrend", checkin);

        List<Map<String, Object>> income = new ArrayList<Map<String, Object>>();
        Map<String, Object> incomeRow = new HashMap<String, Object>();
        incomeRow.put("month", currentMonth());
        incomeRow.put("income", new BigDecimal("12000.50"));
        income.add(incomeRow);
        put("selectIncomeTrend", income);

        List<Map<String, Object>> trend = service.getTrend(6);

        assertEquals("补齐 6 个月", 6, trend.size());
        assertEquals("最后一个月份为当前月", currentMonth(), trend.get(5).get("month"));
        assertEquals("当月入住数", 3L, trend.get(5).get("checkin"));
        assertEquals("当月收入", new BigDecimal("12000.50"), trend.get(5).get("income"));
        assertEquals("缺失月份补 0", 0L, trend.get(0).get("checkin"));
        assertTrue("标签为月份", String.valueOf(trend.get(0).get("label")).endsWith("月"));
    }

    @Test
    public void trendClampsRange()
    {
        List<Map<String, Object>> small = service.getTrend(1);
        List<Map<String, Object>> large = service.getTrend(24);
        assertEquals("最少 3 个月", 3, small.size());
        assertEquals("最多 12 个月", 12, large.size());
    }

    @Test
    public void distributionConvertsTextLabels()
    {
        List<Map<String, Object>> care = new ArrayList<Map<String, Object>>();
        Map<String, Object> careRow = new HashMap<String, Object>();
        careRow.put("careLevel", "2");
        careRow.put("value", 12L);
        care.add(careRow);
        Map<String, Object> unknown = new HashMap<String, Object>();
        unknown.put("careLevel", null);
        unknown.put("value", 1L);
        care.add(unknown);
        put("selectCareLevelDistribution", care);

        List<Map<String, Object>> floors = new ArrayList<Map<String, Object>>();
        Map<String, Object> floor = new HashMap<String, Object>();
        floor.put("floorNo", 2);
        floor.put("total", 20L);
        floor.put("occupied", 17L);
        floors.add(floor);
        put("selectFloorOccupancy", floors);

        List<Map<String, Object>> billTypes = new ArrayList<Map<String, Object>>();
        Map<String, Object> billType = new HashMap<String, Object>();
        billType.put("billType", "1");
        billType.put("value", 30L);
        billType.put("amount", new BigDecimal("80000"));
        billTypes.add(billType);
        put("selectBillTypeDistribution", billTypes);

        Map<String, Object> distribution = service.getDistribution();

        List<Map<String, Object>> careResult = (List<Map<String, Object>>) distribution.get("careLevel");
        assertEquals("半自理文案", "半自理", careResult.get(0).get("name"));
        assertEquals("空等级为未评估", "未评估", careResult.get(1).get("name"));

        List<Map<String, Object>> floorResult = (List<Map<String, Object>>) distribution.get("floorBed");
        assertEquals("楼层名称", "2 层", floorResult.get(0).get("name"));
        assertEquals("空余床位", 3L, floorResult.get(0).get("free"));
        assertEquals("使用率", 85.0, floorResult.get(0).get("rate"));

        List<Map<String, Object>> billResult = (List<Map<String, Object>>) distribution.get("billType");
        assertEquals("月度费用文案", "月度费用", billResult.get(0).get("name"));
        assertNotNull("机构对比数据存在", distribution.get("tenants"));
    }

    @Test
    public void todoReturnsAllSections()
    {
        List<Map<String, Object>> visits = new ArrayList<Map<String, Object>>();
        Map<String, Object> visit = new HashMap<String, Object>();
        visit.put("visitId", 1L);
        visit.put("elderName", "张伟");
        visits.add(visit);
        put("selectPendingVisits", visits);

        Map<String, Object> todo = service.getTodo();

        assertTrue("待办包含探访", todo.containsKey("pendingVisits"));
        assertTrue("待办包含服务", todo.containsKey("unfinishedServices"));
        assertTrue("待办包含欠费", todo.containsKey("unpaidBills"));
        assertTrue("待办包含重点老人", todo.containsKey("keyElders"));
        assertEquals("探访数据透传", 1, ((List<?>) todo.get("pendingVisits")).size());
        assertFalse("服务列表为空时也返回集合", todo.get("unfinishedServices") == null);
    }

    @Test
    public void adminUsesGroupScope()
    {
        loginAs(1L, 1L, "admin");
        Map<String, Object> summary = service.getSummary();
        assertEquals("超级管理员为集团视图", "all", summary.get("scope"));
        assertEquals("集团视图机构名称", "全部机构", summary.get("tenantName"));
    }
}
