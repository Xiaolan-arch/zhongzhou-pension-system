package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.Checkin;
import com.ruoyi.business.domain.HealthReport;
import com.ruoyi.business.domain.vo.BedLocation;

/**
 * AI 健康报告 数据层
 *
 * @author ruoyi
 */
public interface HealthReportMapper
{
    /**
     * 查询报告列表
     */
    public List<HealthReport> selectHealthReportList(HealthReport report);

    /**
     * 通过报告ID查询报告
     */
    public HealthReport selectHealthReportById(Long reportId);

    /**
     * 新增报告
     */
    public int insertHealthReport(HealthReport report);

    /**
     * 更新报告归档地址
     */
    public int updateHealthReportArchive(@Param("reportId") Long reportId, @Param("archivedUrl") String archivedUrl,
            @Param("updateBy") String updateBy);

    /**
     * 批量逻辑删除报告
     */
    public int deleteHealthReportByIds(Long[] reportIds);

    /**
     * 查询老人当前床位位置
     */
    public BedLocation selectBedLocationByElderId(@Param("elderId") Long elderId);

    /**
     * 查询老人最近一次入住申请
     */
    public Checkin selectLatestCheckin(@Param("elderName") String elderName, @Param("idCard") String idCard);

    /**
     * 统计周期内服务订单数
     */
    public int countServiceOrders(@Param("elderId") Long elderId, @Param("start") Date start, @Param("end") Date end);

    /**
     * 统计周期内已完成服务订单数
     */
    public int countCompletedServiceOrders(@Param("elderId") Long elderId, @Param("start") Date start,
            @Param("end") Date end);

    /**
     * 统计周期内服务金额
     */
    public BigDecimal sumServiceAmount(@Param("elderId") Long elderId, @Param("start") Date start,
            @Param("end") Date end);

    /**
     * 统计周期内探访次数
     */
    public int countVisits(@Param("elderId") Long elderId, @Param("start") Date start, @Param("end") Date end);

    /**
     * 统计未结清账单数
     */
    public int countUnpaidBills(@Param("elderId") Long elderId);

    /**
     * 统计未结清金额
     */
    public BigDecimal sumUnpaidBills(@Param("elderId") Long elderId);

    /**
     * 统计已缴费金额
     */
    public BigDecimal sumPaidBills(@Param("elderId") Long elderId);
}
