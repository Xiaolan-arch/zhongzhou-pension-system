package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.mapper.DashboardMapper;
import com.ruoyi.business.service.IDashboardService;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.SysTenant;
import com.ruoyi.system.service.ISysTenantService;

/**
 * 工作台统计 服务实现
 *
 * 数据隔离说明：Mapper 中的 SQL 均为单表或少表查询，
 * 由 TenantSqlInterceptor 依据登录用户自动追加 tenant_id 条件；
 * 超级管理员（user_id = 1）不做过滤，即集团全量视图。
 *
 * @author ruoyi
 */
@Service
public class DashboardServiceImpl implements IDashboardService
{
    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    /** 缓存 key 前缀：dashboard:cache:{scope}:{method}[:{params}] */
    private static final String CACHE_PREFIX = "dashboard:cache:";

    /** 缓存 TTL 90 秒（30s 推送周期 + 60s 冗余） */
    private static final long CACHE_TTL_SECONDS = 90L;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String[] CARE_LEVEL_TEXT = { "未评估", "完全自理", "半自理", "重度失能", "特护" };

    private static final Map<String, String> BILL_TYPE_TEXT = new HashMap<String, String>();

    static
    {
        BILL_TYPE_TEXT.put("0", "首期费用");
        BILL_TYPE_TEXT.put("1", "月度费用");
        BILL_TYPE_TEXT.put("2", "退住结算");
    }

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private ISysTenantService sysTenantService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Map<String, Object> getSummary()
    {
        String key = cacheKey("summary", null);
        Map<String, Object> cached = getFromCache(key, new TypeReference<Map<String, Object>>() {});
        if (cached != null)
        {
            return cached;
        }

        Map<String, Object> data = new LinkedHashMap<String, Object>();

        long elderTotal = nvl(dashboardMapper.countAllElders());
        long inResidence = nvl(dashboardMapper.countInResidenceElders());
        long pendingElders = nvl(dashboardMapper.countPendingElders());
        long bedTotal = nvl(dashboardMapper.countBeds());
        long bedOccupied = nvl(dashboardMapper.countOccupiedBeds());
        BigDecimal monthReceivable = money(dashboardMapper.sumMonthReceivable());
        BigDecimal monthReceived = money(dashboardMapper.sumMonthReceived());
        BigDecimal unpaidTotal = money(dashboardMapper.sumUnpaidAmount());

        data.put("tenantName", resolveTenantName());
        data.put("scope", resolveScope());
        data.put("elderTotal", elderTotal);
        data.put("inResidence", inResidence);
        data.put("pendingElders", pendingElders);
        data.put("bedTotal", bedTotal);
        data.put("bedOccupied", bedOccupied);
        data.put("bedFree", bedTotal - bedOccupied);
        data.put("bedOccupancy", bedTotal == 0 ? 0.0 : Math.round(bedOccupied * 1000.0 / bedTotal) / 10.0);
        data.put("roomTotal", nvl(dashboardMapper.countRooms()));
        data.put("monthReceivable", monthReceivable);
        data.put("monthReceived", monthReceived);
        data.put("monthIncomeRate", monthReceivable.compareTo(BigDecimal.ZERO) == 0
                ? 0.0 : monthReceived.multiply(new BigDecimal("100")).divide(monthReceivable, 1, BigDecimal.ROUND_HALF_UP).doubleValue());
        data.put("unpaidTotal", unpaidTotal);
        data.put("unpaidCount", nvl(dashboardMapper.countUnpaidBills()));
        data.put("overdueCount", nvl(dashboardMapper.countOverdueBills()));
        data.put("overdueAmount", money(dashboardMapper.sumOverdueAmount()));
        data.put("visitPendingToday", nvl(dashboardMapper.countTodayPendingVisits()));
        data.put("visitToday", nvl(dashboardMapper.countTodayVisits()));
        data.put("servicePending", nvl(dashboardMapper.countPendingServiceOrders()));
        data.put("serviceUnfinished", nvl(dashboardMapper.countUnfinishedServiceOrders()));
        data.put("highRiskReports", nvl(dashboardMapper.countHighRiskReports()));

        putCache(key, data);
        return data;
    }

    @Override
    public List<Map<String, Object>> getTrend(int months)
    {
        int size = months < 3 ? 3 : (months > 12 ? 12 : months);
        String key = cacheKey("trend", String.valueOf(size));
        List<Map<String, Object>> cached = getFromCache(key, new TypeReference<List<Map<String, Object>>>() {});
        if (cached != null)
        {
            return cached;
        }

        List<String> monthKeys = buildMonths(size);
        String startMonth = monthKeys.get(0);

        Map<String, Map<String, Object>> income = index(dashboardMapper.selectIncomeTrend(startMonth));
        Map<String, Map<String, Object>> checkin = index(dashboardMapper.selectCheckinTrend(startMonth));
        Map<String, Map<String, Object>> checkout = index(dashboardMapper.selectCheckoutTrend(startMonth));
        Map<String, Map<String, Object>> service = index(dashboardMapper.selectServiceTrend(startMonth));

        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (String month : monthKeys)
        {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("month", month);
            row.put("label", month.substring(5) + "月");
            row.put("checkin", nvl(numberOf(checkin.get(month), "checkin")));
            row.put("checkout", nvl(numberOf(checkout.get(month), "checkout")));
            row.put("serviceDone", nvl(numberOf(service.get(month), "serviceDone")));
            row.put("income", money(numberOf(income.get(month), "income")));
            rows.add(row);
        }
        putCache(key, rows);
        return rows;
    }

    @Override
    public Map<String, Object> getDistribution()
    {
        String key = cacheKey("distribution", null);
        Map<String, Object> cached = getFromCache(key, new TypeReference<Map<String, Object>>() {});
        if (cached != null)
        {
            return cached;
        }

        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("careLevel", convertCareLevel(dashboardMapper.selectCareLevelDistribution()));
        data.put("floorBed", convertFloorBed(dashboardMapper.selectFloorOccupancy()));
        data.put("billType", convertBillType(dashboardMapper.selectBillTypeDistribution()));
        data.put("serviceItem", dashboardMapper.selectServiceItemDistribution());
        data.put("tenants", dashboardMapper.selectTenantOverview());

        putCache(key, data);
        return data;
    }

    @Override
    public Map<String, Object> getTodo()
    {
        String key = cacheKey("todo", null);
        Map<String, Object> cached = getFromCache(key, new TypeReference<Map<String, Object>>() {});
        if (cached != null)
        {
            return cached;
        }

        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("pendingVisits", dashboardMapper.selectPendingVisits(6));
        data.put("unfinishedServices", dashboardMapper.selectUnfinishedOrders(8));
        data.put("unpaidBills", dashboardMapper.selectUnpaidBills(5));
        data.put("keyElders", dashboardMapper.selectKeyElders(5));

        putCache(key, data);
        return data;
    }

    // ==================== 缓存辅助方法 ====================

    /** 拼缓存 key：dashboard:cache:{scope}:{method}[:{params}] */
    private String cacheKey(String method, String params)
    {
        String scope = resolveScopeKey();
        if (params == null || params.isEmpty())
        {
            return CACHE_PREFIX + scope + ":" + method;
        }
        return CACHE_PREFIX + scope + ":" + method + ":" + params;
    }

    /** 构造 scope：admin → all；其他 → tenantId；无登录上下文 → unknown */
    private String resolveScopeKey()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null)
        {
            return "unknown";
        }
        if (SecurityUtils.isAdmin(loginUser.getUserId()))
        {
            return "all";
        }
        Long tenantId = loginUser.getUser().getTenantId();
        return tenantId == null ? "unknown" : String.valueOf(tenantId);
    }

    /** 泛型读缓存：命中则 JSON 反序列化返回；异常或未命中返回 null */
    private <T> T getFromCache(String key, TypeReference<T> type)
    {
        try
        {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json == null || json.isEmpty())
            {
                return null;
            }
            return OBJECT_MAPPER.readValue(json, type);
        }
        catch (Exception e)
        {
            log.warn("Dashboard 缓存读取失败 key={}，降级走 DB：{}", key, e.getMessage());
            return null;
        }
    }

    /** 写缓存：序列化失败只 warn 不影响业务 */
    private void putCache(String key, Object value)
    {
        try
        {
            String json = OBJECT_MAPPER.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            log.warn("Dashboard 缓存写入失败 key={}：{}", key, e.getMessage());
        }
    }

    /** 刷新当前 scope 的全部缓存（供 DashboardPushTask 调用） */
    public void refreshCache()
    {
        String pattern = CACHE_PREFIX + resolveScopeKey() + ":*";
        try
        {
            for (String key : stringRedisTemplate.keys(pattern))
            {
                stringRedisTemplate.delete(key);
            }
        }
        catch (Exception e)
        {
            log.warn("Dashboard 缓存刷新失败：{}", e.getMessage());
        }
    }

    /** 当前登录用户是否集团视角 */
    private String resolveScope()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || SecurityUtils.isAdmin(loginUser.getUserId()))
        {
            return "all";
        }
        return "tenant";
    }

    /** 机构名称（集团视角显示全部机构） */
    private String resolveTenantName()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || SecurityUtils.isAdmin(loginUser.getUserId()))
        {
            return "全部机构";
        }
        Long tenantId = loginUser.getUser().getTenantId();
        if (tenantId == null)
        {
            return "全部机构";
        }
        SysTenant tenant = sysTenantService.selectTenantById(tenantId);
        return tenant == null || tenant.getTenantName() == null ? "未命名机构" : tenant.getTenantName();
    }

    /** 生成连续的月份序列（yyyy-MM） */
    private List<String> buildMonths(int size)
    {
        List<String> months = new ArrayList<String>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, -(size - 1));
        for (int i = 0; i < size; i++)
        {
            months.add(format.format(calendar.getTime()));
            calendar.add(Calendar.MONTH, 1);
        }
        return months;
    }

    /** 将月份聚合结果按 month 建立索引 */
    private Map<String, Map<String, Object>> index(List<Map<String, Object>> rows)
    {
        Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();
        if (rows == null)
        {
            return result;
        }
        for (Map<String, Object> row : rows)
        {
            Object month = row.get("month");
            if (month != null)
            {
                result.put(month.toString(), row);
            }
        }
        return result;
    }

    private Object numberOf(Map<String, Object> row, String key)
    {
        return row == null ? null : row.get(key);
    }

    private List<Map<String, Object>> convertCareLevel(List<Map<String, Object>> rows)
    {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (rows == null)
        {
            return result;
        }
        for (Map<String, Object> row : rows)
        {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            String level = String.valueOf(row.get("careLevel") == null ? "" : row.get("careLevel"));
            int index = 0;
            try
            {
                index = level.isEmpty() ? 0 : Integer.parseInt(level);
            }
            catch (NumberFormatException ignored)
            {
                index = 0;
            }
            item.put("name", index >= 0 && index < CARE_LEVEL_TEXT.length ? CARE_LEVEL_TEXT[index] : "未评估");
            item.put("value", nvl(numberOf(row, "value")));
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> convertFloorBed(List<Map<String, Object>> rows)
    {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (rows == null)
        {
            return result;
        }
        for (Map<String, Object> row : rows)
        {
            long total = nvl(numberOf(row, "total"));
            long occupied = nvl(numberOf(row, "occupied"));
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("name", String.valueOf(row.get("floorNo") == null ? "--" : row.get("floorNo")) + " 层");
            item.put("total", total);
            item.put("occupied", occupied);
            item.put("free", total - occupied);
            item.put("rate", total == 0 ? 0.0 : Math.round(occupied * 1000.0 / total) / 10.0);
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> convertBillType(List<Map<String, Object>> rows)
    {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (rows == null)
        {
            return result;
        }
        for (Map<String, Object> row : rows)
        {
            String type = String.valueOf(row.get("billType") == null ? "" : row.get("billType"));
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("name", BILL_TYPE_TEXT.containsKey(type) ? BILL_TYPE_TEXT.get(type) : "其他费用");
            item.put("value", nvl(numberOf(row, "value")));
            item.put("amount", money(numberOf(row, "amount")));
            result.add(item);
        }
        return result;
    }

    private long nvl(Object value)
    {
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    private BigDecimal money(Object value)
    {
        if (value instanceof BigDecimal)
        {
            return (BigDecimal) value;
        }
        if (value instanceof Number)
        {
            return new BigDecimal(((Number) value).toString());
        }
        return BigDecimal.ZERO;
    }
}
