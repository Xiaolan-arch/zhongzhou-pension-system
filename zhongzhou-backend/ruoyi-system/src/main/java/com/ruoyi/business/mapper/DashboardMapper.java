package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 工作台统计 数据层
 *
 * 所有查询均为单表或少表聚合，租户过滤由 TenantSqlInterceptor 自动追加
 *
 * @author ruoyi
 */
public interface DashboardMapper
{
    /** 老人总数 */
    public Long countAllElders();

    /** 在住老人数 */
    public Long countInResidenceElders();

    /** 待入住老人数 */
    public Long countPendingElders();

    /** 床位数 */
    public Long countBeds();

    /** 已入住床位数 */
    public Long countOccupiedBeds();

    /** 房间数 */
    public Long countRooms();

    /** 本月应收费用 */
    public BigDecimal sumMonthReceivable();

    /** 本月实收费用 */
    public BigDecimal sumMonthReceived();

    /** 未结清金额 */
    public BigDecimal sumUnpaidAmount();

    /** 未结清账单数 */
    public Long countUnpaidBills();

    /** 逾期账单数（超过 period_end 30 天未付清） */
    public Long countOverdueBills();

    /** 逾期账单金额合计 */
    public BigDecimal sumOverdueAmount();

    /** 今日待确认探访 */
    public Long countTodayPendingVisits();

    /** 今日探访总数 */
    public Long countTodayVisits();

    /** 待执行服务订单数 */
    public Long countPendingServiceOrders();

    /** 未完成服务订单数（待执行 + 已执行） */
    public Long countUnfinishedServiceOrders();

    /** 高风险健康报告数 */
    public Long countHighRiskReports();

    /** 月度收入趋势 */
    public List<Map<String, Object>> selectIncomeTrend(@Param("startMonth") String startMonth);

    /** 月度入住趋势 */
    public List<Map<String, Object>> selectCheckinTrend(@Param("startMonth") String startMonth);

    /** 月度退住趋势 */
    public List<Map<String, Object>> selectCheckoutTrend(@Param("startMonth") String startMonth);

    /** 月度服务完成趋势 */
    public List<Map<String, Object>> selectServiceTrend(@Param("startMonth") String startMonth);

    /** 在住老人护理等级分布 */
    public List<Map<String, Object>> selectCareLevelDistribution();

    /** 各楼层床位占用 */
    public List<Map<String, Object>> selectFloorOccupancy();

    /** 账单类型分布 */
    public List<Map<String, Object>> selectBillTypeDistribution();

    /** 服务项目排行 */
    public List<Map<String, Object>> selectServiceItemDistribution();

    /** 各机构对比（集团视图） */
    public List<Map<String, Object>> selectTenantOverview();

    /** 待处理探访 */
    public List<Map<String, Object>> selectPendingVisits(@Param("limit") int limit);

    /** 未完成服务订单 */
    public List<Map<String, Object>> selectUnfinishedOrders(@Param("limit") int limit);

    /** 欠费账单 */
    public List<Map<String, Object>> selectUnpaidBills(@Param("limit") int limit);

    /** 重点关注老人（重度失能/特护） */
    public List<Map<String, Object>> selectKeyElders(@Param("limit") int limit);
}
