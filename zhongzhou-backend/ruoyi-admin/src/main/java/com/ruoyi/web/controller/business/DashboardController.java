package com.ruoyi.web.controller.business;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.service.IDashboardLayoutService;
import com.ruoyi.business.service.IDashboardService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 工作台统计
 *
 * 首页所有登录用户可访问，数据范围由租户拦截器自动控制
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/dashboard")
public class DashboardController extends BaseController
{
    @Autowired
    private IDashboardService dashboardService;

    @Autowired
    private IDashboardLayoutService dashboardLayoutService;

    /**
     * 核心指标
     */
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        return success(dashboardService.getSummary());
    }

    /**
     * 近 N 个月趋势
     */
    @GetMapping("/trend")
    public AjaxResult trend(@RequestParam(defaultValue = "6") int months)
    {
        return success(dashboardService.getTrend(months));
    }

    /**
     * 分布数据
     */
    @GetMapping("/distribution")
    public AjaxResult distribution()
    {
        return success(dashboardService.getDistribution());
    }

    /**
     * 今日待办
     */
    @GetMapping("/todo")
    public AjaxResult todo()
    {
        return success(dashboardService.getTodo());
    }

    /**
     * 当前用户的工作台布局配置
     */
    @GetMapping("/layout")
    public AjaxResult layout()
    {
        return success().put("layout", dashboardLayoutService.getLayout());
    }

    /**
     * 保存当前用户的工作台布局配置
     */
    @PutMapping("/layout")
    public AjaxResult saveLayout(@RequestBody Map<String, Object> body)
    {
        Object layout = body == null ? null : body.get("layout");
        dashboardLayoutService.saveLayout(layout == null ? null : String.valueOf(layout));
        return success();
    }
}
