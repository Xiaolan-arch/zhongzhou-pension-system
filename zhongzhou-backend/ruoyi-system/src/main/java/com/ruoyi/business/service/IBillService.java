package com.ruoyi.business.service;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.business.domain.Bill;
import com.ruoyi.business.domain.BillItem;
import com.ruoyi.business.domain.PaymentRecord;

/**
 * 账单 服务层
 */
public interface IBillService
{
    public List<Bill> selectBillList(Bill bill);

    public Bill selectBillById(Long billId);

    public List<BillItem> selectBillItemsByBillId(Long billId);

    public int insertBill(Bill bill);

    /** 生成月度账单 */
    public int generateBill(String billMonth);

    /**
     * 账单缴费（支持部分收款）
     * 前端传入：billId, payAmount（本次金额）, payMethod, payVoucher, remark
     * 后端自动计算 paidAmount 累计值和 payStatus
     */
    public int payBill(Bill bill);

    /**
     * 账单退款（支持部分退款）
     * 前端传入：billId, refundAmount（本次退款金额）, remark（退款原因）
     * 后端扣减 paidAmount 并重新计算 payStatus，同时写入一条负数收款记录
     */
    public int refundBill(Long billId, BigDecimal refundAmount, String reason);

    public int deleteBillByIds(Long[] billIds);

    /** 计算并填充账单的逾期天数与滞纳金（overdueDays / penaltyAmount） */
    public void fillPenalty(Bill bill);

    /** 查询指定账单的所有收款记录 */
    public List<PaymentRecord> selectPaymentRecords(Long billId);

    /** 查询逾期账单列表（超过 periodEnd 30 天未付清） */
    public List<Bill> selectOverdueBills(Long tenantId);

    /**
     * 重新计算所有逾期未结清账单的滞纳金并落库
     *
     * @return 更新的账单条数
     */
    public int refreshOverduePenalty();
}
