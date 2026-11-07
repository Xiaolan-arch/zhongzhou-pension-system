package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.Bill;
import com.ruoyi.business.domain.BillItem;

/**
 * 账单 数据层
 *
 * @author ruoyi
 */
public interface BillMapper
{
    /**
     * 查询账单列表
     *
     * @param bill 账单
     * @return 账单集合
     */
    public List<Bill> selectBillList(Bill bill);

    /**
     * 通过账单ID查询账单
     *
     * @param billId 账单ID
     * @return 账单
     */
    public Bill selectBillById(Long billId);

    /**
     * 查询账单明细
     *
     * @param billId 账单ID
     * @return 账单明细集合
     */
    public List<BillItem> selectBillItemsByBillId(Long billId);

    /**
     * 新增账单
     *
     * @param bill 账单
     * @return 结果
     */
    public int insertBill(Bill bill);

    /**
     * 批量新增账单（月度账单生成使用）
     *
     * @param list 账单集合
     * @return 实际插入条数
     */
    public int insertBillBatch(@Param("list") List<Bill> list);

    /**
     * 查询该机构该月份已生成的月度账单所关联的入住申请ID集合（用于生成前过滤，保证幂等）
     *
     * @param tenantId 机构ID
     * @param billMonth 账单月份
     * @return 已存在的入住申请ID集合
     */
    public List<Long> selectExistingCheckinIds(@Param("tenantId") Long tenantId, @Param("billMonth") String billMonth);

    /**
     * 查询该机构该月份已生成的月度账单的老人ID集合（用于 checkinId 为空的历史数据兜底判断）
     *
     * @param tenantId 机构ID
     * @param billMonth 账单月份
     * @return 已存在的老人ID集合
     */
    public List<Long> selectExistingElderIds(@Param("tenantId") Long tenantId, @Param("billMonth") String billMonth);

    /**
     * 更新缴费信息
     *
     * @param bill 账单
     * @return 结果
     */
    public int updateBillPay(Bill bill);

    /**
     * 更新退款信息（乐观锁：where paid_amount = 旧已收金额）
     *
     * @param bill 账单（billId、paidAmount、payStatus、oldPaidAmount）
     * @return 结果
     */
    public int updateBillRefund(Bill bill);

    /**
     * 批量逻辑删除账单
     *
     * @param billIds 账单ID数组
     * @return 结果
     */
    public int deleteBillByIds(Long[] billIds);

    /**
     * 查询该月份尚未生成月度账单的在住老人
     *
     * @param tenantId 机构ID
     * @param billMonth 账单月份
     * @return 老人账单基础信息（elderId、elderName、tenantId）
     */
    public List<Bill> selectEldersForMonthlyBill(@Param("tenantId") Long tenantId, @Param("billMonth") String billMonth);

    /**
     * 查询入住申请的首期账单
     *
     * @param checkinId 入住申请ID
     * @return 首期账单
     */
    public Bill selectFirstBillByCheckinId(@Param("checkinId") Long checkinId);

    /**
     * 统计老人未结清账单数量
     *
     * @param elderId 老人ID
     * @param tenantId 机构ID
     * @return 未结清账单数量
     */
    public int selectUnpaidCountByElder(@Param("elderId") Long elderId, @Param("tenantId") Long tenantId);

    /**
     * 查询逾期账单（超过 periodEnd 30 天未付清）
     *
     * @param tenantId 机构ID（admin 传 null 查全部）
     * @return 逾期账单集合
     */
    public List<Bill> selectOverdueBills(@Param("tenantId") Long tenantId);

    /**
     * 查询账单应付剩余金额
     *
     * @param billId 账单ID
     * @return 应付剩余（可能为负表示多付）
     */
    public BigDecimal selectBillPayable(@Param("billId") Long billId);

    /**
     * 查询逾期且未结清的账单（滞纳金定时重算使用）
     *
     * @param graceDays 宽限期天数
     * @return 账单集合
     */
    public List<Bill> selectBillsForPenalty(@Param("graceDays") int graceDays);

    /**
     * 写入滞纳金计算结果
     *
     * @param bill 账单（billId、penaltyAmount）
     * @return 结果
     */
    public int updateBillPenalty(Bill bill);
}
