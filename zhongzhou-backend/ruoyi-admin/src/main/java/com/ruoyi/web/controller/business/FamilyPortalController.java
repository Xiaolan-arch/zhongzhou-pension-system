package com.ruoyi.web.controller.business;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.Bill;
import com.ruoyi.business.domain.CareLog;
import com.ruoyi.business.domain.Checkin;
import com.ruoyi.business.domain.FamilyMember;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.domain.Feedback;
import com.ruoyi.business.domain.HealthRecord;
import com.ruoyi.business.domain.HealthReport;
import com.ruoyi.business.domain.Invoice;
import com.ruoyi.business.domain.Leave;
import com.ruoyi.business.domain.Medication;
import com.ruoyi.business.domain.ServiceItem;
import com.ruoyi.business.domain.ServiceOrder;
import com.ruoyi.business.domain.Visit;
import com.ruoyi.business.domain.vo.FamilyElderVO;
import com.ruoyi.business.service.ICareLogService;
import com.ruoyi.business.service.IFamilyAuthService;
import com.ruoyi.business.service.IFamilyPortalService;
import com.ruoyi.business.service.IFeedbackService;
import com.ruoyi.business.service.IInvoiceService;
import com.ruoyi.business.service.ILeaveService;
import com.ruoyi.business.service.IMedicationService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;

/**
 * 家属端业务查询接口（只读为主 + 探访预约）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/family")
public class FamilyPortalController
{
    @Autowired
    private IFamilyAuthService familyAuthService;

    @Autowired
    private IFamilyPortalService familyPortalService;

    @Autowired
    private ILeaveService leaveService;

    @Autowired
    private IMedicationService medicationService;

    @Autowired
    private ICareLogService careLogService;

    @Autowired
    private IFeedbackService feedbackService;

    @Autowired
    private IInvoiceService invoiceService;

    private FamilyToken auth(String token)
    {
        return familyAuthService.authenticate(token);
    }

    /**
     * 绑定的老人列表
     */
    @GetMapping("/elders")
    public AjaxResult elders(@RequestHeader(value = "X-Family-Token", required = false) String token)
    {
        List<FamilyElderVO> elders = familyPortalService.selectElders(auth(token));
        return AjaxResult.success(elders);
    }

    /**
     * 老人档案详情
     */
    @GetMapping("/elders/{elderId}")
    public AjaxResult elderDetail(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @PathVariable Long elderId)
    {
        return AjaxResult.success(familyPortalService.selectElderDetail(auth(token), elderId));
    }

    /**
     * 服务记录
     */
    @GetMapping("/services")
    public AjaxResult services(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestParam Long elderId)
    {
        List<ServiceOrder> list = familyPortalService.selectServices(auth(token), elderId);
        return AjaxResult.success(list);
    }

    /**
     * 账单列表
     */
    @GetMapping("/bills")
    public AjaxResult bills(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestParam Long elderId)
    {
        List<Bill> list = familyPortalService.selectBills(auth(token), elderId);
        return AjaxResult.success(list);
    }

    /**
     * 账单详情（含明细）
     */
    @GetMapping("/bills/{billId}")
    public AjaxResult billDetail(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @PathVariable Long billId)
    {
        return AjaxResult.success(familyPortalService.selectBillDetail(auth(token), billId));
    }

    /**
     * 在线缴费（演示环境为模拟支付，不真实调用微信支付）
     */
    @PostMapping("/bills/{billId}/pay")
    public AjaxResult payBill(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @PathVariable Long billId, @RequestBody(required = false) Map<String, Object> body)
    {
        BigDecimal amount = null;
        Object raw = body == null ? null : body.get("amount");
        if (raw != null && String.valueOf(raw).trim().length() > 0)
        {
            try
            {
                amount = new BigDecimal(String.valueOf(raw).trim());
            }
            catch (Exception e)
            {
                throw new ServiceException("缴费金额格式不正确");
            }
        }
        Bill bill = familyPortalService.payBill(auth(token), billId, amount);
        return AjaxResult.success(bill);
    }

    /**
     * 健康报告列表
     */
    @GetMapping("/reports")
    public AjaxResult reports(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestParam Long elderId)
    {
        List<HealthReport> list = familyPortalService.selectReports(auth(token), elderId);
        return AjaxResult.success(list);
    }

    /**
     * 健康报告详情
     */
    @GetMapping("/reports/{reportId}")
    public AjaxResult reportDetail(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @PathVariable Long reportId)
    {
        return AjaxResult.success(familyPortalService.selectReportDetail(auth(token), reportId));
    }

    /**
     * 查询家属对指定服务订单的评价
     */
    @GetMapping("/services/{orderId}/evaluation")
    public AjaxResult serviceEvaluation(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @PathVariable Long orderId)
    {
        return AjaxResult.success(familyPortalService.selectServiceEvaluation(auth(token), orderId));
    }

    /**
     * 提交服务评价
     */
    @PostMapping("/services/{orderId}/evaluation")
    public AjaxResult submitServiceEvaluation(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @PathVariable Long orderId, @RequestBody(required = false) Map<String, Object> body)
    {
        Integer rating = null;
        Object rawRating = body == null ? null : body.get("rating");
        if (rawRating != null && String.valueOf(rawRating).trim().length() > 0)
        {
            try
            {
                rating = Integer.valueOf(String.valueOf(rawRating).trim());
            }
            catch (Exception e)
            {
                throw new ServiceException("满意度参数不正确");
            }
        }
        Object rawContent = body == null ? null : body.get("content");
        String content = rawContent == null ? null : String.valueOf(rawContent);
        return AjaxResult.success(familyPortalService.submitServiceEvaluation(auth(token), orderId, rating, content));
    }

    /**
     * 押金余额与收退流水
     */
    @GetMapping("/deposits")
    public AjaxResult deposits(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestParam Long elderId)
    {
        return AjaxResult.success(familyPortalService.selectDeposits(auth(token), elderId));
    }

    /**
     * 消息通知列表
     */
    @GetMapping("/messages")
    public AjaxResult messages(@RequestHeader(value = "X-Family-Token", required = false) String token)
    {
        return AjaxResult.success(familyPortalService.selectMessages(auth(token)));
    }

    /**
     * 探访预约列表
     */
    @GetMapping("/visits")
    public AjaxResult visits(@RequestHeader(value = "X-Family-Token", required = false) String token)
    {
        List<Visit> list = familyPortalService.selectVisits(auth(token));
        return AjaxResult.success(list);
    }

    /**
     * 提交探访预约
     */
    @PostMapping("/visits")
    public AjaxResult createVisit(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestBody Visit visit)
    {
        return AjaxResult.success(familyPortalService.createVisit(auth(token), visit));
    }

    /**
     * 取消探访预约
     */
    @PutMapping("/visits/{visitId}/cancel")
    public AjaxResult cancelVisit(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @PathVariable Long visitId)
    {
        familyPortalService.cancelVisit(auth(token), visitId);
        return AjaxResult.success();
    }

    // ====================== 公开接口（无需 token） ======================

    /**
     * 公开-服务项目列表（仅上架，固定 tenant_id=1 中州养老院）
     */
    @GetMapping("/public/service-items")
    public AjaxResult publicServiceItems()
    {
        List<ServiceItem> list = familyPortalService.selectPublicServiceItems();
        return AjaxResult.success(list);
    }

    /**
     * 公开-服务项目详情
     */
    @GetMapping("/public/service-items/{itemId}")
    public AjaxResult publicServiceItemDetail(@PathVariable Long itemId)
    {
        return AjaxResult.success(familyPortalService.selectPublicServiceItemById(itemId));
    }

    /**
     * 公开-房型列表（含空床数聚合）
     */
    @GetMapping("/public/rooms")
    public AjaxResult publicRooms()
    {
        List<Map<String, Object>> list = familyPortalService.selectPublicRooms();
        return AjaxResult.success(list);
    }

    /**
     * 公开-房型详情（含床位状态列表）
     */
    @GetMapping("/public/rooms/{roomId}")
    public AjaxResult publicRoomDetail(@PathVariable Long roomId)
    {
        return AjaxResult.success(familyPortalService.selectPublicRoomDetail(roomId));
    }

    // ====================== 入住申请（需 token） ======================

    /**
     * 提交入住申请（创建草稿并返回申请单号）
     */
    @PostMapping("/checkins")
    public AjaxResult createCheckin(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestBody Checkin checkin)
    {
        return AjaxResult.success(familyPortalService.createCheckin(auth(token), checkin));
    }

    /**
     * 我的入住申请列表
     */
    @GetMapping("/checkins")
    public AjaxResult checkins(@RequestHeader(value = "X-Family-Token", required = false) String token)
    {
        List<Checkin> list = familyPortalService.selectCheckins(auth(token));
        return AjaxResult.success(list);
    }

    /**
     * 入住申请详情（校验归属）
     */
    @GetMapping("/checkins/{checkinId}")
    public AjaxResult checkinDetail(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @PathVariable Long checkinId)
    {
        return AjaxResult.success(familyPortalService.selectCheckinDetail(auth(token), checkinId));
    }

    /**
     * 撤销入住申请（仅申请中/待评估/已驳回态可撤）
     */
    @PutMapping("/checkins/{checkinId}/cancel")
    public AjaxResult cancelCheckin(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @PathVariable Long checkinId)
    {
        familyPortalService.cancelCheckin(auth(token), checkinId);
        return AjaxResult.success();
    }

    /**
     * 家属在线确认入住合同签约
     */
    @PostMapping("/checkins/{checkinId}/sign")
    public AjaxResult signCheckin(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @PathVariable Long checkinId)
    {
        familyPortalService.signCheckinContract(auth(token), checkinId);
        return AjaxResult.success();
    }

    // ====================== 绑定/解绑老人 ======================

    /**
     * 绑定前按姓名匹配老人档案（用于确认 / 同名选择）
     */
    @GetMapping("/bindings/match")
    public AjaxResult matchBindings(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestParam String elderName)
    {
        return AjaxResult.success(familyPortalService.matchElders(auth(token), elderName));
    }

    /**
     * 发起绑定（按姓名匹配老人档案：已存在则关联，不存在则自动创建新档案后绑定）
     */
    @PostMapping("/bindings")
    public AjaxResult bindElder(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestBody Map<String, Object> body)
    {
        String elderName = body.get("elderName") == null ? null : body.get("elderName").toString();
        String phone = body.get("phone") == null ? null : body.get("phone").toString();
        String relation = (String) body.getOrDefault("relation", "家属");
        Long elderId = null;
        Object rawElderId = body.get("elderId");
        if (rawElderId != null && String.valueOf(rawElderId).trim().length() > 0)
        {
            elderId = Long.valueOf(String.valueOf(rawElderId).trim());
        }
        return AjaxResult.success(familyPortalService.bindElder(auth(token), elderName, phone, relation, elderId));
    }

    /**
     * 我的绑定列表
     */
    @GetMapping("/bindings")
    public AjaxResult bindings(@RequestHeader(value = "X-Family-Token", required = false) String token)
    {
        List<FamilyMember> list = familyPortalService.selectBindings(auth(token));
        return AjaxResult.success(list);
    }

    /**
     * 解绑
     */
    @PutMapping("/bindings/{elderId}/unbind")
    public AjaxResult unbind(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @PathVariable Long elderId)
    {
        familyPortalService.unbindElder(auth(token), elderId);
        return AjaxResult.success();
    }

    // ====================== 服务下单 ======================

    /**
     * 预约下单
     */
    @PostMapping("/orders")
    public AjaxResult createOrder(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestBody Map<String, Object> body)
    {
        FamilyToken family = auth(token);
        Long elderId = Long.valueOf(body.get("elderId").toString());
        Long itemId = Long.valueOf(body.get("itemId").toString());
        String remark = body.get("remark") == null ? null : body.get("remark").toString();
        Date serviceTime = null;
        if (body.get("serviceTime") != null)
        {
            try
            {
                serviceTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .parse(body.get("serviceTime").toString());
            }
            catch (Exception ignore)
            {
            }
        }
        return AjaxResult.success(familyPortalService.createOrder(family, elderId, itemId, serviceTime, remark));
    }

    /**
     * 取消未开始订单
     */
    @PutMapping("/orders/{orderId}/cancel")
    public AjaxResult cancelOrder(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @PathVariable Long orderId)
    {
        familyPortalService.cancelOrder(auth(token), orderId);
        return AjaxResult.success();
    }

    // ====================== 体征查询 ======================

    /**
     * 最近 N 天体征
     */
    @GetMapping("/health-records/recent")
    public AjaxResult recentHealth(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestParam Long elderId,
            @RequestParam(defaultValue = "7") int days)
    {
        List<HealthRecord> list = familyPortalService.selectRecentHealth(auth(token), elderId, days);
        return AjaxResult.success(list);
    }

    // ====================== 请假外出 ======================

    @GetMapping("/leaves")
    public AjaxResult leaves(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestParam Long elderId)
    {
        return AjaxResult.success(leaveService.selectFamilyLeaves(auth(token), elderId));
    }

    @PostMapping("/leaves")
    public AjaxResult createLeave(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestBody Leave leave)
    {
        return AjaxResult.success(leaveService.createFamilyLeave(auth(token), leave));
    }

    @PutMapping("/leaves/{leaveId}/cancel")
    public AjaxResult cancelLeave(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @PathVariable Long leaveId)
    {
        leaveService.cancelFamilyLeave(auth(token), leaveId);
        return AjaxResult.success();
    }

    // ====================== 用药提醒 ======================

    @GetMapping("/medications")
    public AjaxResult medications(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestParam Long elderId)
    {
        return AjaxResult.success(medicationService.selectFamilyMedications(auth(token), elderId));
    }

    // ====================== 护理日志 ======================

    @GetMapping("/care-logs")
    public AjaxResult careLogs(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestParam Long elderId)
    {
        return AjaxResult.success(careLogService.selectFamilyCareLogs(auth(token), elderId));
    }

    // ====================== 投诉建议 ======================

    @GetMapping("/feedback")
    public AjaxResult feedbacks(@RequestHeader(value = "X-Family-Token", required = false) String token)
    {
        return AjaxResult.success(feedbackService.selectFamilyFeedbacks(auth(token)));
    }

    @PostMapping("/feedback")
    public AjaxResult createFeedback(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestBody Feedback feedback)
    {
        return AjaxResult.success(feedbackService.createFamilyFeedback(auth(token), feedback));
    }

    // ====================== 发票申请 ======================

    @GetMapping("/invoices")
    public AjaxResult invoices(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestParam Long elderId)
    {
        return AjaxResult.success(invoiceService.selectFamilyInvoices(auth(token), elderId));
    }

    @PostMapping("/invoices")
    public AjaxResult createInvoice(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestBody Invoice invoice)
    {
        return AjaxResult.success(invoiceService.createFamilyInvoice(auth(token), invoice));
    }
}
