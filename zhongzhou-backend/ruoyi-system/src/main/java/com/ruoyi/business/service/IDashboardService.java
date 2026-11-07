package com.ruoyi.business.service;

import java.util.List;
import java.util.Map;

/**
 * 工作台统计 服务层
 *
 * @author ruoyi
 */
public interface IDashboardService
{
    /**
     * 核心指标
     *
     * @return 指标集合
     */
    public Map<String, Object> getSummary();

    /**
     * 近 N 个月趋势（入住/退住/收入/服务完成）
     *
     * @param months 月份数量（3-12）
     * @return 趋势集合
     */
    public List<Map<String, Object>> getTrend(int months);

    /**
     * 分布数据（护理等级/楼层床位/账单类型/服务项目/机构对比）
     *
     * @return 分布集合
     */
    public Map<String, Object> getDistribution();

    /**
     * 今日待办
     *
     * @return 待办集合
     */
    public Map<String, Object> getTodo();
}
