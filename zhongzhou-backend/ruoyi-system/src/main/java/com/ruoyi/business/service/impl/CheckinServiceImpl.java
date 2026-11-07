package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.Bill;
import com.ruoyi.business.domain.BillItem;
import com.ruoyi.business.domain.Checkin;
import com.ruoyi.business.mapper.BillItemMapper;
import com.ruoyi.business.mapper.BillMapper;
import com.ruoyi.business.mapper.CheckinBedMapper;
import com.ruoyi.business.mapper.CheckinMapper;
import com.ruoyi.business.service.ICheckinService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 入住申请 服务层处理
 *
 * 状态机（单一真相：apply_status）：
 *   0 申请中 → 1 待评估 → 2 待审核 → [审批] → 3 待配置 → 4 待签约 → 5 待缴费 → [complete] → 6 已入住
 *                                                                            ↘ [驳回] → 9 已驳回 → [重新提交] → 2 待审核
 *
 * 闭环规则：
 *   - submitStep(step>=3) 必须已经过 audit 审核通过（auditResult='0'），否则抛异常强制走审批
 *   - audit 通过后自动把状态从 2 推到 3
 *   - complete 只有 applyStatus='5' 且 payStatus='1' 才能走，内部自动占用床位+创建老人档案+生成首期账单
 */
@Service
public class CheckinServiceImpl implements ICheckinService
{
    /** 首期押金 */
    private static final BigDecimal DEPOSIT_AMOUNT = new BigDecimal("1000.00");

    /** apply_status 常量，避免散落硬编码 */
    private static final String S_DRAFT       = "0";  // 申请中
    private static final String S_ASSESSING   = "1";  // 待评估
    private static final String S_PENDING_AUDIT = "2";// 待审核
    private static final String S_CONFIG      = "3";  // 待配置（审批通过后）
    private static final String S_CONTRACT    = "4";  // 待签约
    private static final String S_PAY         = "5";  // 待缴费
    private static final String S_DONE        = "6";  // 已入住
    private static final String S_REJECTED    = "9";  // 已驳回

    @Autowired
    private CheckinMapper checkinMapper;

    @Autowired
    private CheckinBedMapper checkinBedMapper;

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private BillItemMapper billItemMapper;

    @Override
    public List<Checkin> selectCheckinList(Checkin checkin)
    {
        return checkinMapper.selectCheckinList(checkin);
    }

    @Override
    public Checkin selectCheckinById(Long checkinId)
    {
        return checkinMapper.selectCheckinById(checkinId);
    }

    @Override
    public int insertCheckin(Checkin checkin)
    {
        checkin.setApplyNo("ZD" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
        checkin.setApplyStatus(S_DRAFT);
        checkin.setPayStatus("0");
        // 管理端由 Spring Security 上下文补充租户与操作人；家属端无该上下文，沿用调用方已设置的值
        if (checkin.getTenantId() == null)
        {
            checkin.setTenantId(SecurityUtils.getTenantId());
        }
        if (checkin.getCreateBy() == null || checkin.getCreateBy().trim().length() == 0)
        {
            checkin.setCreateBy(SecurityUtils.getUsername());
        }
        return checkinMapper.insertCheckin(checkin);
    }

    @Override
    public int updateCheckin(Checkin checkin)
    {
        checkin.setUpdateBy(SecurityUtils.getUsername());
        return checkinMapper.updateCheckin(checkin);
    }

    /**
     * 向导步骤提交（核心审批拦截逻辑在此）
     *
     * 步骤映射：
     *   step=1 → 状态 0→1（基础信息 → 待评估）
     *   step=2 → 状态 1→2（评估 → 待审核）  ← 向导结束，必须走审批
     *   step=3 → 状态 2→3（床位/护理配置）  ← 拦截点：必须已审核通过
     *   step=4 → 状态 3→4（签约）
     *   step=5 → 状态 4→5（缴费）
     *   step=6 → 状态 5 首期缴费处理（不推进状态）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitStep(Long checkinId, Integer step, Checkin checkin)
    {
        if (step == null || step < 1 || step > 6)
        {
            throw new ServiceException("步骤参数错误（1-6）");
        }
        Checkin current = checkinMapper.selectCheckinById(checkinId);
        if (current == null)
        {
            throw new ServiceException("入住申请不存在");
        }

        // 闭环拦截：step >= 3（期望推进到 3 及以后）时必须已过审核
        if (step >= 3)
        {
            if (S_PENDING_AUDIT.equals(current.getApplyStatus()))
            {
                // 还在待审核状态，不允许越过审批
                throw new ServiceException("当前申请处于【待审核】状态，请先由机构管理员审批通过后再继续办理");
            }
            if (StringUtils.isBlank(current.getAuditResult())
                    || !"0".equals(current.getAuditResult()))
            {
                throw new ServiceException("入住申请尚未完成审核，不能继续办理");
            }
            if (S_REJECTED.equals(current.getApplyStatus()))
            {
                throw new ServiceException("申请已被驳回，请点击【重新提交】后再办理");
            }
        }

        String expectStatus = String.valueOf(step - 1);
        // 驳回后 resubmit 会把状态重置为 2，此时向导 step=3 期望状态可能对不上
        // 只在 step 1/2 严格校验状态，step>=3 只要已经过审核就放行
        if (step <= 2 && !expectStatus.equals(current.getApplyStatus()))
        {
            throw new ServiceException("状态已变更，请刷新后重试（当前：" + current.getApplyStatus() + "，期望：" + expectStatus + "）");
        }

        String username = SecurityUtils.getUsername();
        checkin.setCheckinId(checkinId);
        checkin.setUpdateBy(username);
        checkinMapper.updateCheckin(checkin);

        if (step <= 5)
        {
            int rows = checkinMapper.updateCheckinStatus(checkinId, current.getApplyStatus(),
                    String.valueOf(step), username);
            if (rows == 0)
            {
                throw new ServiceException("状态已变更，请刷新后重试");
            }
            return 1;
        }

        // step = 6：首期缴费，状态保持待缴费（5），创建首期账单并标记已付
        Checkin latest = checkinMapper.selectCheckinById(checkinId);
        if (latest == null)
        {
            throw new ServiceException("入住申请不存在");
        }
        if (latest.getFirstBillId() == null)
        {
            Bill firstBill = billMapper.selectFirstBillByCheckinId(checkinId);
            if (firstBill == null)
            {
                firstBill = createFirstBill(latest, username);
            }
            latest.setFirstBillId(firstBill.getBillId());
            latest.setPayStatus("1");
            latest.setUpdateBy(username);
            checkinMapper.updateCheckinFirstPay(latest);
        }
        return 1;
    }

    /**
     * 入住审核（仅待审核状态可操作）
     * 审核通过：状态 2→3 待配置，自动推进到审批后阶段
     * 审核驳回：状态 2→9 已驳回
     */
    @Override
    public int audit(Checkin checkin)
    {
        if (checkin == null || checkin.getCheckinId() == null)
        {
            throw new ServiceException("入住申请ID不能为空");
        }
        Checkin current = checkinMapper.selectCheckinById(checkin.getCheckinId());
        if (current == null)
        {
            throw new ServiceException("入住申请不存在");
        }
        if (!S_PENDING_AUDIT.equals(current.getApplyStatus()))
        {
            throw new ServiceException("当前状态（" + current.getApplyStatus() + "）不允许审核，仅【待审核】状态可操作");
        }
        if (checkin.getAuditResult() == null
                || (!"0".equals(checkin.getAuditResult()) && !"1".equals(checkin.getAuditResult())))
        {
            throw new ServiceException("审核结果不正确（0通过 / 1驳回）");
        }
        // 驳回必须填意见
        if ("1".equals(checkin.getAuditResult())
                && StringUtils.isBlank(checkin.getAuditOpinion()))
        {
            throw new ServiceException("驳回时审核意见不能为空");
        }
        // 通过 → 自动推进到 3 待配置（审批后第一个业务节点）
        String newStatus = "0".equals(checkin.getAuditResult()) ? S_CONFIG : S_REJECTED;
        checkin.setAuditor(SecurityUtils.getUsername());
        checkin.setUpdateBy(SecurityUtils.getUsername());
        int rows = checkinMapper.updateCheckinAudit(checkin, newStatus);
        if (rows == 0)
        {
            throw new ServiceException("状态已变更，请刷新后重试");
        }
        return 1;
    }

    /**
     * 驳回后重新提交（9 已驳回 → 2 待审核）
     * 自动清空旧审核字段，申请人可再次走向导修改后提交审批
     */
    @Override
    public int resubmit(Long checkinId)
    {
        if (checkinId == null)
        {
            throw new ServiceException("入住申请ID不能为空");
        }
        Checkin current = checkinMapper.selectCheckinById(checkinId);
        if (current == null)
        {
            throw new ServiceException("入住申请不存在");
        }
        if (!S_REJECTED.equals(current.getApplyStatus()))
        {
            throw new ServiceException("当前状态（" + current.getApplyStatus() + "）不允许重新提交，仅【已驳回】状态可操作");
        }
        String username = SecurityUtils.getUsername();
        int rows = checkinMapper.updateCheckinResubmit(checkinId, username);
        if (rows == 0)
        {
            throw new ServiceException("状态已变更，请刷新后重试");
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int complete(Long checkinId)
    {
        Checkin checkin = checkinMapper.selectCheckinById(checkinId);
        if (checkin == null)
        {
            throw new ServiceException("入住申请不存在");
        }
        // 闭环双重校验：必须已过审批 → 状态在 5 待缴费
        if (!S_PAY.equals(checkin.getApplyStatus()) || !"1".equals(checkin.getPayStatus()))
        {
            throw new ServiceException("当前状态（" + checkin.getApplyStatus() + "/" + checkin.getPayStatus() + "）不允许完成入住，请先完成审批→床位→签约→缴费流程");
        }
        if (checkin.getBedId() == null)
        {
            throw new ServiceException("请先分配床位");
        }
        String username = SecurityUtils.getUsername();
        checkin.setUpdateBy(username);
        if (checkin.getCheckinDate() == null)
        {
            checkin.setCheckinDate(new Date());
        }
        Long elderId = resolveElderId(checkin, username);
        int bedRows = checkinBedMapper.occupyBed(checkin.getBedId(), elderId, checkin.getTenantId());
        if (bedRows == 0)
        {
            throw new ServiceException("床位已被占用");
        }
        checkinBedMapper.updateElderByCheckin(elderId, checkin);
        int rows = checkinMapper.updateCheckinComplete(checkin);
        if (rows == 0)
        {
            throw new ServiceException("状态已变更，请刷新后重试");
        }
        return 1;
    }

    @Override
    public int deleteCheckinByIds(Long[] checkinIds)
    {
        return checkinMapper.deleteCheckinByIds(checkinIds);
    }

    /**
     * 解析老人ID，不存在则按入住申请快照新增
     */
    private Long resolveElderId(Checkin checkin, String username)
    {
        Long tenantId = checkin.getTenantId();
        Long elderId = null;
        if (StringUtils.isNotEmpty(checkin.getIdCard()))
        {
            elderId = checkinBedMapper.selectElderIdByIdCard(checkin.getIdCard(), tenantId);
        }
        if (elderId == null && StringUtils.isNotEmpty(checkin.getElderName()))
        {
            elderId = checkinBedMapper.selectElderIdByName(checkin.getElderName(), tenantId);
        }
        if (elderId == null)
        {
            checkin.setCreateBy(username);
            checkinBedMapper.insertElderByCheckin(checkin);
            if (StringUtils.isNotEmpty(checkin.getIdCard()))
            {
                elderId = checkinBedMapper.selectElderIdByIdCard(checkin.getIdCard(), tenantId);
            }
            else
            {
                elderId = checkinBedMapper.selectElderIdByName(checkin.getElderName(), tenantId);
            }
        }
        return elderId;
    }

    /**
     * 创建首期账单及明细
     */
    private Bill createFirstBill(Checkin checkin, String username)
    {
        Long elderId = resolveElderId(checkin, username);
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null)
        {
            tenantId = checkin.getTenantId();
        }
        BigDecimal fee = checkin.getFeeStandard() == null ? BigDecimal.ZERO : checkin.getFeeStandard();
        LocalDate startDate = checkin.getCheckinDate() == null
                ? LocalDate.now()
                : checkin.getCheckinDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        String billMonth = YearMonth.now().toString();

        Bill bill = new Bill();
        bill.setBillNo("ZD" + billMonth.replace("-", "") + checkin.getCheckinId());
        bill.setElderId(elderId);
        bill.setElderName(checkin.getElderName());
        bill.setCheckinId(checkin.getCheckinId());
        bill.setBillMonth(billMonth);
        bill.setPeriodStart(toDate(startDate));
        bill.setPeriodEnd(toDate(endDate));
        bill.setAmount(fee.add(DEPOSIT_AMOUNT));
        bill.setDeposit(DEPOSIT_AMOUNT);
        bill.setPrepaid(BigDecimal.ZERO);
        bill.setPaidAmount(fee.add(DEPOSIT_AMOUNT));
        bill.setPayStatus("1");
        bill.setPayTime(new Date());
        bill.setBillType("0");
        bill.setTenantId(tenantId);
        bill.setCreateBy(username);
        billMapper.insertBill(bill);

        BillItem feeItem = new BillItem();
        feeItem.setBillId(bill.getBillId());
        feeItem.setItemType("1");
        feeItem.setFeeName("护理费用");
        feeItem.setServiceContent(checkin.getCareLevel());
        feeItem.setAmount(fee);
        feeItem.setTenantId(tenantId);
        billItemMapper.insertBillItem(feeItem);

        BillItem depositItem = new BillItem();
        depositItem.setBillId(bill.getBillId());
        depositItem.setItemType("1");
        depositItem.setFeeName("押金");
        depositItem.setServiceContent("入住押金");
        depositItem.setAmount(DEPOSIT_AMOUNT);
        depositItem.setTenantId(tenantId);
        billItemMapper.insertBillItem(depositItem);

        return bill;
    }

    private static Date toDate(LocalDate localDate)
    {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
