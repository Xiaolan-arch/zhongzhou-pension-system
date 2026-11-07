package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.audit.BizAudit;
import com.ruoyi.business.config.BillOverdueProperties;
import com.ruoyi.business.domain.Bill;
import com.ruoyi.business.domain.BillItem;
import com.ruoyi.business.domain.PaymentRecord;
import com.ruoyi.business.mapper.BillMapper;
import com.ruoyi.business.mapper.PaymentRecordMapper;
import com.ruoyi.business.service.IBillService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 账单 服务层处理
 */
@Service
public class BillServiceImpl implements IBillService
{
    /** 月度账单默认金额（兜底：当 checkin.fee_standard 取不到时使用） */
    private static final BigDecimal DEFAULT_MONTHLY_AMOUNT = new BigDecimal("2600.00");

    /** 批量插入每批条数（避免单条 INSERT 过长） */
    private static final int BATCH_INSERT_SIZE = 500;

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private PaymentRecordMapper paymentRecordMapper;

    @Autowired
    private BillOverdueProperties overdueProperties;

    @Override
    public List<Bill> selectBillList(Bill bill)
    {
        List<Bill> list = billMapper.selectBillList(bill);
        for (Bill item : list)
        {
            fillPenalty(item);
        }
        return list;
    }

    @Override
    public Bill selectBillById(Long billId)
    {
        Bill bill = billMapper.selectBillById(billId);
        fillPenalty(bill);
        return bill;
    }

    @Override
    public List<BillItem> selectBillItemsByBillId(Long billId)
    {
        return billMapper.selectBillItemsByBillId(billId);
    }

    @Override
    public int insertBill(Bill bill)
    {
        bill.setTenantId(SecurityUtils.getTenantId());
        if (bill.getCreateBy() == null)
        {
            bill.setCreateBy(SecurityUtils.getUsername());
        }
        return billMapper.insertBill(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateBill(String billMonth)
    {
        YearMonth yearMonth;
        try
        {
            yearMonth = YearMonth.parse(billMonth);
        }
        catch (Exception e)
        {
            throw new ServiceException("账单月份格式不正确");
        }
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null)
        {
            throw new ServiceException("机构信息异常");
        }
        String username = SecurityUtils.getUsername();

        List<Bill> elders = billMapper.selectEldersForMonthlyBill(tenantId, billMonth);

        // 幂等：先查该机构该月份已生成的月度账单，用入住申请ID / 老人ID 集合过滤，避免依赖唯一索引异常
        Set<Long> existingCheckinIds = new HashSet<>(billMapper.selectExistingCheckinIds(tenantId, billMonth));
        Set<Long> existingElderIds = new HashSet<>(billMapper.selectExistingElderIds(tenantId, billMonth));

        List<Bill> billList = new ArrayList<>(elders.size());
        for (Bill elder : elders)
        {
            if (elder.getCheckinId() != null)
            {
                // 同一入住记录同一月份已生成账单，跳过
                if (existingCheckinIds.contains(elder.getCheckinId()))
                {
                    continue;
                }
            }
            else if (existingElderIds.contains(elder.getElderId()))
            {
                // 历史数据缺少入住记录ID时，用 elderId + billMonth 兜底判断是否已生成
                continue;
            }

            // 取入住记录里的费用标准，取不到则用默认值兜底
            BigDecimal amount = elder.getMonthlyFee();
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            {
                amount = DEFAULT_MONTHLY_AMOUNT;
            }

            Bill bill = new Bill();
            bill.setBillNo("ZD" + billMonth.replace("-", "") + elder.getElderId());
            bill.setElderId(elder.getElderId());
            bill.setElderName(elder.getElderName());
            bill.setCheckinId(elder.getCheckinId());
            bill.setBillMonth(billMonth);
            bill.setPeriodStart(toDate(firstDay));
            bill.setPeriodEnd(toDate(lastDay));
            bill.setAmount(amount);
            bill.setDeposit(BigDecimal.ZERO);
            bill.setPrepaid(BigDecimal.ZERO);
            bill.setPaidAmount(BigDecimal.ZERO);
            bill.setPayStatus("0");
            bill.setBillType("1");
            bill.setTenantId(tenantId != null ? tenantId : elder.getTenantId());
            bill.setCreateBy(username);
            billList.add(bill);
        }

        // 批量插入：每 BATCH_INSERT_SIZE 条一批，避免单条 SQL 过长
        int count = 0;
        for (int i = 0; i < billList.size(); i += BATCH_INSERT_SIZE)
        {
            List<Bill> batch = billList.subList(i, Math.min(i + BATCH_INSERT_SIZE, billList.size()));
            count += billMapper.insertBillBatch(batch);
        }
        return count;
    }

    /**
     * 账单缴费（支持部分收款）
     * 传入字段：billId, payAmount（本次金额）, payMethod, payVoucher, remark
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @BizAudit(value = "BILL_PAY", action = "账单收款")
    public int payBill(Bill bill)
    {
        if (bill == null || bill.getBillId() == null)
        {
            throw new ServiceException("账单ID不能为空");
        }
        Bill current = billMapper.selectBillById(bill.getBillId());
        if (current == null)
        {
            throw new ServiceException("账单不存在");
        }
        if ("2".equals(current.getPayStatus()))
        {
            throw new ServiceException("账单已退款，不能收款");
        }

        // 确定本次收款金额：未传则默认 = 应付剩余
        BigDecimal payAmount = bill.getPayAmount();
        BigDecimal payable = billMapper.selectBillPayable(bill.getBillId());
        if (payable == null)
        {
            payable = BigDecimal.ZERO;
        }
        if (payAmount == null || payAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            payAmount = payable.compareTo(BigDecimal.ZERO) > 0 ? payable : BigDecimal.ZERO;
        }
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("收款金额必须大于0");
        }
        if (payAmount.compareTo(payable) > 0)
        {
            throw new ServiceException("收款金额（" + payAmount + "）超过应付剩余（" + payable + "）");
        }

        // 计算新的累计已付
        BigDecimal oldPaid = current.getPaidAmount() == null ? BigDecimal.ZERO : current.getPaidAmount();
        BigDecimal newPaid = oldPaid.add(payAmount);

        // 自动判断支付状态：累计已付 >= 账单金额 → 已付清，否则部分已收
        String newStatus;
        BigDecimal totalOwing = current.getAmount() == null ? BigDecimal.ZERO : current.getAmount();
        // 判断是否付清：应付剩余 - 本次金额 <= 0 （即 本次金额 >= 应付剩余）
        if (payAmount.compareTo(payable) >= 0 || newPaid.compareTo(totalOwing) >= 0)
        {
            newStatus = "1"; // 已付清
        }
        else
        {
            newStatus = "3"; // 部分已收
        }

        // 组装更新对象，保留旧值做乐观锁
        Bill update = new Bill();
        update.setBillId(bill.getBillId());
        update.setPaidAmount(newPaid);
        update.setPayStatus(newStatus);
        update.setPayMethod(bill.getPayMethod());
        update.setPayVoucher(bill.getPayVoucher());
        update.setOldPaidAmount(oldPaid);
        update.setUpdateBy(SecurityUtils.getUsername());

        int rows = billMapper.updateBillPay(update);
        if (rows == 0)
        {
            throw new ServiceException("账单已被其他操作更新，请刷新后重试");
        }

        // 插入收款记录
        PaymentRecord record = new PaymentRecord();
        record.setRecordNo(generateRecordNo());
        record.setBillId(current.getBillId());
        record.setBillNo(current.getBillNo());
        record.setElderId(current.getElderId());
        record.setElderName(current.getElderName());
        record.setPayAmount(payAmount);
        record.setPayMethod(bill.getPayMethod());
        record.setPayVoucher(bill.getPayVoucher());
        record.setRemark(bill.getRemark());
        record.setOperatorId(SecurityUtils.getUserId());
        record.setOperatorName(SecurityUtils.getUsername());
        record.setTenantId(current.getTenantId());
        record.setCreateBy(SecurityUtils.getUsername());
        paymentRecordMapper.insertPaymentRecord(record);

        return rows;
    }

    /**
     * 账单退款（支持部分退款）
     * 传入字段：billId, refundAmount（本次退款金额）, reason（退款原因）
     * 退款后重新计算支付状态：已收金额为 0 → 应收（'0'）；未收齐 → 部分已收（'3'）；收齐 → 已付清（'1'）
     * '2'（已退款）仅用于整单退款语义，此处不设置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @BizAudit(value = "BILL_REFUND", action = "账单退款")
    public int refundBill(Long billId, BigDecimal refundAmount, String reason)
    {
        if (billId == null)
        {
            throw new ServiceException("账单ID不能为空");
        }
        Bill current = billMapper.selectBillById(billId);
        if (current == null)
        {
            throw new ServiceException("账单不存在");
        }

        BigDecimal oldPaid = current.getPaidAmount() == null ? BigDecimal.ZERO : current.getPaidAmount();
        if (oldPaid.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("该账单无收款记录，无法退款");
        }
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("退款金额必须大于0");
        }
        if (refundAmount.compareTo(oldPaid) > 0)
        {
            throw new ServiceException("退款金额超过已收金额");
        }

        // 计算新的累计已收
        BigDecimal newPaid = oldPaid.subtract(refundAmount);

        // 自动判断支付状态：0 → 应收，未收齐 → 部分已收，收齐 → 已付清
        BigDecimal totalOwing = current.getAmount() == null ? BigDecimal.ZERO : current.getAmount();
        String newStatus;
        if (newPaid.compareTo(BigDecimal.ZERO) == 0)
        {
            newStatus = "0"; // 应收
        }
        else if (newPaid.compareTo(totalOwing) < 0)
        {
            newStatus = "3"; // 部分已收
        }
        else
        {
            newStatus = "1"; // 已付清
        }

        // 组装更新对象，保留旧值做乐观锁
        Bill update = new Bill();
        update.setBillId(billId);
        update.setPaidAmount(newPaid);
        update.setPayStatus(newStatus);
        update.setOldPaidAmount(oldPaid);
        update.setUpdateBy(SecurityUtils.getUsername());

        int rows = billMapper.updateBillRefund(update);
        if (rows == 0)
        {
            throw new ServiceException("账单已被其他操作更新，请刷新后重试");
        }

        // 写入负数收款记录，作为退款流水
        PaymentRecord record = new PaymentRecord();
        record.setRecordNo(generateRecordNo());
        record.setBillId(current.getBillId());
        record.setBillNo(current.getBillNo());
        record.setElderId(current.getElderId());
        record.setElderName(current.getElderName());
        record.setPayAmount(refundAmount.negate());
        record.setPayMethod("退款");
        record.setRemark(reason);
        record.setOperatorId(SecurityUtils.getUserId());
        record.setOperatorName(SecurityUtils.getUsername());
        record.setTenantId(current.getTenantId());
        record.setCreateBy(SecurityUtils.getUsername());
        paymentRecordMapper.insertPaymentRecord(record);

        return rows;
    }

    @Override
    public int deleteBillByIds(Long[] billIds)
    {
        return billMapper.deleteBillByIds(billIds);
    }

    @Override
    public List<PaymentRecord> selectPaymentRecords(Long billId)
    {
        return paymentRecordMapper.selectPaymentRecordsByBillId(billId);
    }

    @Override
    public List<Bill> selectOverdueBills(Long tenantId)
    {
        List<Bill> list = billMapper.selectOverdueBills(tenantId);
        for (Bill item : list)
        {
            fillPenalty(item);
        }
        return list;
    }

    /**
     * 重新计算逾期未结清账单的滞纳金并落库（每日定时任务 / 前端手动触发）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int refreshOverduePenalty()
    {
        if (!overdueProperties.isEnabled())
        {
            return 0;
        }
        List<Bill> list = billMapper.selectBillsForPenalty(overdueProperties.getGraceDays());
        int count = 0;
        for (Bill item : list)
        {
            fillPenalty(item);
            Bill update = new Bill();
            update.setBillId(item.getBillId());
            update.setPenaltyAmount(item.getPenaltyAmount());
            count += billMapper.updateBillPenalty(update);
        }
        return count;
    }

    /**
     * 计算逾期天数与滞纳金（写入非数据库字段 overdueDays / penaltyAmount）
     * 滞纳金 = 未缴金额 × 日费率 × 逾期天数，四舍五入到分
     */
    @Override
    public void fillPenalty(Bill bill)
    {
        if (bill == null)
        {
            return;
        }
        if (!overdueProperties.isEnabled())
        {
            bill.setOverdueDays(0);
            bill.setPenaltyAmount(BigDecimal.ZERO);
            return;
        }
        int days = calcOverdueDays(bill.getPeriodEnd());
        bill.setOverdueDays(days);
        if (days <= 0 || "1".equals(bill.getPayStatus()) || "2".equals(bill.getPayStatus()))
        {
            bill.setPenaltyAmount(BigDecimal.ZERO);
            return;
        }
        BigDecimal unpaid = nvl(bill.getAmount()).subtract(nvl(bill.getPrepaid())).subtract(nvl(bill.getPaidAmount()));
        if (unpaid.compareTo(BigDecimal.ZERO) <= 0)
        {
            bill.setPenaltyAmount(BigDecimal.ZERO);
            return;
        }
        BigDecimal penalty = unpaid.multiply(overdueProperties.getDailyRate())
                .multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP);
        bill.setPenaltyAmount(penalty);
    }

    /** 逾期天数：周期结束后超过宽限期的天数 */
    private int calcOverdueDays(Date periodEnd)
    {
        if (periodEnd == null)
        {
            return 0;
        }
        LocalDate end = periodEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate deadline = end.plusDays(overdueProperties.getGraceDays());
        long days = ChronoUnit.DAYS.between(deadline, LocalDate.now());
        return days > 0 ? (int) days : 0;
    }

    private static BigDecimal nvl(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    /** 收款单本地递增计数器（单机永不回退） */
    private static final AtomicLong RECORD_SEQ = new AtomicLong(0);

    /** 生成收款单号：PAY + yyyyMMddHHmmssSSS + 6位递增序号（单机永不碰撞） */
    private static String generateRecordNo()
    {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS");
        String datePart = sdf.format(new Date());
        long seq = RECORD_SEQ.incrementAndGet() % 1000000;
        return "PAY" + datePart + String.format("%06d", seq);
    }

    private static Date toDate(LocalDate localDate)
    {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
