package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.Bill;
import com.ruoyi.business.domain.BillItem;
import com.ruoyi.business.domain.Checkin;
import com.ruoyi.business.domain.Deposit;
import com.ruoyi.business.domain.FamilyMember;
import com.ruoyi.business.domain.HealthReport;
import com.ruoyi.business.domain.MessageLog;
import com.ruoyi.business.domain.ServiceOrder;
import com.ruoyi.business.domain.Visit;
import com.ruoyi.business.domain.vo.FamilyElderVO;
/**
 * 家属端数据层（按手机号 + 机构ID限定数据范围）
 *
 * @author ruoyi
 */
public interface FamilyMapper
{
    /**
     * 按手机号查询家属绑定关系
     */
    public List<FamilyMember> selectFamiliesByPhone(@Param("phone") String phone);

    /**
     * 查询家属绑定的在住老人列表
     */
    public List<FamilyElderVO> selectEldersByPhone(@Param("phone") String phone, @Param("tenantId") Long tenantId);

    /**
     * 查询家属绑定的单个老人档案
     */
    public FamilyElderVO selectElderDetail(@Param("phone") String phone, @Param("tenantId") Long tenantId,
            @Param("elderId") Long elderId);

    /**
     * 查询老人服务记录
     */
    public List<ServiceOrder> selectServiceOrders(@Param("elderId") Long elderId, @Param("tenantId") Long tenantId);

    /**
     * 查询单条服务订单（评价归属与状态校验用）
     */
    public ServiceOrder selectServiceOrder(@Param("orderId") Long orderId, @Param("tenantId") Long tenantId);

    /**
     * 查询老人账单
     */
    public List<Bill> selectBills(@Param("elderId") Long elderId, @Param("tenantId") Long tenantId);

    /**
     * 查询账单详情
     */
    public Bill selectBillById(@Param("billId") Long billId, @Param("tenantId") Long tenantId);

    /**
     * 查询账单明细
     */
    public List<BillItem> selectBillItems(@Param("billId") Long billId, @Param("tenantId") Long tenantId);

    /**
     * 查询老人健康报告列表（不含正文）
     */
    public List<HealthReport> selectReports(@Param("elderId") Long elderId, @Param("tenantId") Long tenantId);

    /**
     * 查询健康报告详情
     */
    public HealthReport selectReportById(@Param("reportId") Long reportId, @Param("tenantId") Long tenantId);

    /**
     * 查询家属提交的探访预约
     */
    public List<Visit> selectVisitsByPhone(@Param("phone") String phone, @Param("tenantId") Long tenantId);

    /**
     * 取消本人提交的探访预约
     */
    public int cancelVisit(@Param("visitId") Long visitId, @Param("phone") String phone,
            @Param("tenantId") Long tenantId);

    /**
     * 查询老人押金收退流水
     */
    public List<Deposit> selectDeposits(@Param("elderId") Long elderId, @Param("tenantId") Long tenantId);

    /**
     * 查询老人押金余额（收押金 - 退押金）
     */
    public java.math.BigDecimal selectDepositBalance(@Param("elderId") Long elderId, @Param("tenantId") Long tenantId);

    /**
     * 查询家属收到的消息通知
     */
    public List<MessageLog> selectMessages(@Param("phone") String phone, @Param("tenantId") Long tenantId);

    /**
     * 查询某位老人的全部家属（消息推送用）
     */
    public List<FamilyMember> selectFamiliesByElder(@Param("tenantId") Long tenantId, @Param("elderId") Long elderId);

    /**
     * 按手机号更新微信 openid
     */
    public int updateOpenidByPhone(@Param("phone") String phone, @Param("openid") String openid);

    /**
     * 家属端在线缴费：更新账单已付金额与支付状态
     */
    public int updateBillPay(@Param("billId") Long billId, @Param("tenantId") Long tenantId,
            @Param("newPaid") java.math.BigDecimal newPaid, @Param("payStatus") String payStatus,
            @Param("oldPaid") java.math.BigDecimal oldPaid, @Param("updateBy") String updateBy);

    // ====================== 入住申请（家属视角） ======================

    /**
     * 家属-入住申请列表（按家属手机号+租户过滤）
     */
    public List<Checkin> selectCheckinsByFamily(@Param("familyPhone") String familyPhone,
            @Param("tenantId") Long tenantId);

    /**
     * 家属-入住申请详情（校验归属：checkin_id + family_phone + tenant_id）
     */
    public Checkin selectCheckinDetailByFamily(@Param("checkinId") Long checkinId,
            @Param("familyPhone") String familyPhone, @Param("tenantId") Long tenantId);

    /**
     * 家属-撤销入住申请（仅申请中/待评估/已驳回态可撤）
     */
    public int cancelCheckinByFamily(@Param("checkinId") Long checkinId,
            @Param("familyPhone") String familyPhone, @Param("tenantId") Long tenantId);

    /**
     * 家属-在线确认签约（仅待签约态可签，签约后进入待缴费）
     */
    public int signCheckinByFamily(@Param("checkinId") Long checkinId,
            @Param("familyPhone") String familyPhone, @Param("tenantId") Long tenantId);

    // ====================== 绑定/解绑（家属视角） ======================

    /** 家属端：按手机号查已绑定老人列表 */
    public List<FamilyMember> selectBindingsByFamily(@Param("phone") String phone,
            @Param("tenantId") Long tenantId);

    /** 家属端：按姓名匹配本机构已登记老人（可能同名多条，供用户确认选择） */
    public List<FamilyElderVO> selectEldersForBinding(@Param("tenantId") Long tenantId,
            @Param("elderName") String elderName);

    /** 家属端：按老人ID查询可绑定档案（校验机构归属） */
    public FamilyElderVO selectElderForBindingById(@Param("tenantId") Long tenantId,
            @Param("elderId") Long elderId);

    /** 家属端：查是否已绑定 */
    public FamilyMember selectBindingByFamilyAndElder(@Param("phone") String phone,
            @Param("elderId") Long elderId, @Param("tenantId") Long tenantId);

    /** 家属端：新增绑定 */
    public int insertFamilyMember(FamilyMember member);

    /** 家属端：恢复已解绑的关系（避免唯一键冲突） */
    public int reactivateBinding(@Param("familyId") Long familyId, @Param("relation") String relation);

    /** 家属端：解绑（把 status 改成 1 停用） */
    public int unbindByFamilyAndElder(@Param("phone") String phone,
            @Param("elderId") Long elderId, @Param("tenantId") Long tenantId);
}
