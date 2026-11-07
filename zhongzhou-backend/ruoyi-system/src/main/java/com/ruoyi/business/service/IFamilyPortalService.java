package com.ruoyi.business.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.ruoyi.business.domain.Bill;
import com.ruoyi.business.domain.Checkin;
import com.ruoyi.business.domain.FamilyMember;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.domain.HealthRecord;
import com.ruoyi.business.domain.HealthReport;
import com.ruoyi.business.domain.Room;
import com.ruoyi.business.domain.ServiceItem;
import com.ruoyi.business.domain.ServiceOrder;
import com.ruoyi.business.domain.Visit;
import com.ruoyi.business.domain.vo.FamilyElderVO;

/**
 * 家属端业务查询服务
 *
 * @author ruoyi
 */
public interface IFamilyPortalService
{
    /**
     * 家属绑定的老人列表
     */
    public List<FamilyElderVO> selectElders(FamilyToken family);

    /**
     * 老人档案详情
     */
    public FamilyElderVO selectElderDetail(FamilyToken family, Long elderId);

    /**
     * 服务记录
     */
    public List<ServiceOrder> selectServices(FamilyToken family, Long elderId);

    /**
     * 账单列表
     */
    public List<Bill> selectBills(FamilyToken family, Long elderId);

    /**
     * 账单详情（含明细）
     */
    public Map<String, Object> selectBillDetail(FamilyToken family, Long billId);

    /**
     * 健康报告列表
     */
    public List<HealthReport> selectReports(FamilyToken family, Long elderId);

    /**
     * 健康报告详情
     */
    public HealthReport selectReportDetail(FamilyToken family, Long reportId);

    /**
     * 押金余额与收退流水
     */
    public Map<String, Object> selectDeposits(FamilyToken family, Long elderId);

    /**
     * 家属收到的消息通知
     */
    public List<com.ruoyi.business.domain.MessageLog> selectMessages(FamilyToken family);

    /**
     * 查询家属对指定服务订单的评价
     */
    public Map<String, Object> selectServiceEvaluation(FamilyToken family, Long orderId);

    /**
     * 提交服务评价（仅已完成的订单，同一订单同一家属仅可评价一次）
     */
    public com.ruoyi.business.domain.ServiceEvaluation submitServiceEvaluation(FamilyToken family, Long orderId,
            Integer rating, String content);

    /**
     * 探访预约列表
     */
    public List<Visit> selectVisits(FamilyToken family);

    /**
     * 提交探访预约
     */
    public Visit createVisit(FamilyToken family, Visit visit);

    /**
     * 取消探访预约
     */
    public int cancelVisit(FamilyToken family, Long visitId);

    /**
     * 家属端在线缴费（演示环境为模拟支付，不调用微信支付）
     *
     * @param family 登录家属
     * @param billId 账单ID
     * @param amount 缴费金额（为空则缴清剩余应付）
     * @return 缴费后的账单
     */
    public Bill payBill(FamilyToken family, Long billId, java.math.BigDecimal amount);

    // ====================== 公开接口（无需 token） ======================

    /**
     * 公开-服务项目列表（仅上架，固定 tenant_id=1）
     */
    public List<ServiceItem> selectPublicServiceItems();

    /**
     * 公开-服务项目详情
     */
    public ServiceItem selectPublicServiceItemById(Long itemId);

    /**
     * 公开-房型列表（含空床数聚合）
     */
    public List<Map<String, Object>> selectPublicRooms();

    /**
     * 公开-房型详情（含床位状态）
     */
    public Map<String, Object> selectPublicRoomDetail(Long roomId);

    // ====================== 入住申请（需 token） ======================

    /**
     * 提交入住申请（创建草稿并返回）
     */
    public Checkin createCheckin(FamilyToken family, Checkin checkin);

    /**
     * 我的入住申请列表（按家属手机号+租户过滤）
     */
    public List<Checkin> selectCheckins(FamilyToken family);

    /**
     * 入住申请详情（校验归属）
     */
    public Checkin selectCheckinDetail(FamilyToken family, Long checkinId);

    /**
     * 撤销入住申请（仅申请中/待评估/已驳回态可撤）
     */
    public int cancelCheckin(FamilyToken family, Long checkinId);

    /**
     * 家属-在线确认入住合同签约
     */
    public int signCheckinContract(FamilyToken family, Long checkinId);

    // ====================== 绑定/解绑老人 ======================

    /**
     * 按姓名匹配本机构老人档案（绑定前预览确认用，可能返回多条同名档案）
     */
    public List<FamilyElderVO> matchElders(FamilyToken family, String elderName);

    /**
     * 发起绑定老人（按姓名匹配本机构档案，已存在则关联，不存在则自动创建新档案后绑定）
     *
     * @param elderId 可选：用户在同名档案中选定的老人ID
     */
    public FamilyMember bindElder(FamilyToken family, String elderName, String phone, String relation,
            Long elderId);

    /**
     * 我的绑定列表
     */
    public List<FamilyMember> selectBindings(FamilyToken family);

    /**
     * 解绑
     */
    public int unbindElder(FamilyToken family, Long elderId);

    // ====================== 服务下单 ======================

    /**
     * 家属端预约下单
     */
    public ServiceOrder createOrder(FamilyToken family, Long elderId, Long itemId,
            java.util.Date serviceTime, String remark);

    /**
     * 取消未开始订单
     */
    public int cancelOrder(FamilyToken family, Long orderId);

    // ====================== 体征查询 ======================

    /**
     * 最近 N 天体征
     */
    public List<HealthRecord> selectRecentHealth(FamilyToken family, Long elderId, int days);
}
