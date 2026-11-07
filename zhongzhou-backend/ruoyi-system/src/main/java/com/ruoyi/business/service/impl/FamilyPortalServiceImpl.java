package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.Bed;
import com.ruoyi.business.domain.Bill;
import com.ruoyi.business.domain.BillItem;
import com.ruoyi.business.domain.Checkin;
import com.ruoyi.business.domain.Deposit;
import com.ruoyi.business.domain.Elder;
import com.ruoyi.business.domain.FamilyMember;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.domain.HealthRecord;
import com.ruoyi.business.domain.HealthReport;
import com.ruoyi.business.domain.MessageLog;
import com.ruoyi.business.domain.PaymentRecord;
import com.ruoyi.business.domain.Room;
import com.ruoyi.business.domain.ServiceEvaluation;
import com.ruoyi.business.domain.ServiceItem;
import com.ruoyi.business.domain.ServiceOrder;
import com.ruoyi.business.domain.Visit;
import com.ruoyi.business.domain.vo.FamilyElderVO;
import com.ruoyi.business.mapper.BedMapper;
import com.ruoyi.business.mapper.ElderMapper;
import com.ruoyi.business.mapper.FamilyMapper;
import com.ruoyi.business.mapper.HealthRecordMapper;
import com.ruoyi.business.mapper.PaymentRecordMapper;
import com.ruoyi.business.mapper.ServiceEvaluationMapper;
import com.ruoyi.business.mapper.ServiceOrderMapper;
import com.ruoyi.business.mapper.VisitMapper;
import com.ruoyi.business.service.IBillService;
import com.ruoyi.business.service.ICheckinService;
import com.ruoyi.business.service.IFamilyPortalService;
import com.ruoyi.business.service.IRoomService;
import com.ruoyi.business.service.IServiceItemService;
import com.ruoyi.common.exception.ServiceException;

/**
 * 家属端业务查询服务实现（所有查询均限定机构ID + 家属绑定关系）
 *
 * @author ruoyi
 */
@Service
public class FamilyPortalServiceImpl implements IFamilyPortalService
{
    /** 单次预约人数上限 */
    private static final int MAX_PEOPLE_COUNT = 5;

    /** 服务评价内容长度上限 */
    private static final int MAX_EVALUATION_CONTENT = 500;

    /** 服务订单已完成状态 */
    private static final String ORDER_STATUS_FINISHED = "2";

    /** 公开接口默认展示的租户（Demo：中州养老院） */
    private static final Long DEMO_TENANT_ID = 1L;

    /** 入住申请状态：申请中 */
    private static final String CHECKIN_STATUS_APPLYING = "0";

    /** 入住申请状态：已驳回/已撤销（家属端可撤销的终态/待态） */
    private static final String CHECKIN_STATUS_REJECTED = "9";

    @Autowired
    private FamilyMapper familyMapper;

    @Autowired
    private VisitMapper visitMapper;

    @Autowired
    private PaymentRecordMapper paymentRecordMapper;

    @Autowired
    private ServiceEvaluationMapper serviceEvaluationMapper;

    @Autowired
    private IBillService billService;

    @Autowired
    private IServiceItemService serviceItemService;

    @Autowired
    private IRoomService roomService;

    @Autowired
    private ICheckinService checkinService;

    @Autowired
    private BedMapper bedMapper;

    @Autowired
    private ElderMapper elderMapper;

    @Autowired
    private HealthRecordMapper healthRecordMapper;

    @Autowired
    private ServiceOrderMapper serviceOrderMapper;

    /** 收款单号自增序列 */
    private static final AtomicLong RECORD_SEQ = new AtomicLong(0);

    @Override
    public List<FamilyElderVO> selectElders(FamilyToken family)
    {
        return familyMapper.selectEldersByPhone(family.getPhone(), family.getTenantId());
    }

    @Override
    public FamilyElderVO selectElderDetail(FamilyToken family, Long elderId)
    {
        if (elderId == null)
        {
            throw new ServiceException("老人ID不能为空");
        }
        FamilyElderVO elder = familyMapper.selectElderDetail(family.getPhone(), family.getTenantId(), elderId);
        if (elder == null)
        {
            throw new ServiceException("无权查看该老人信息");
        }
        return elder;
    }

    @Override
    public List<ServiceOrder> selectServices(FamilyToken family, Long elderId)
    {
        checkElderOwnership(family, elderId);
        return familyMapper.selectServiceOrders(elderId, family.getTenantId());
    }

    @Override
    public List<Bill> selectBills(FamilyToken family, Long elderId)
    {
        checkElderOwnership(family, elderId);
        List<Bill> bills = familyMapper.selectBills(elderId, family.getTenantId());
        for (Bill bill : bills)
        {
            billService.fillPenalty(bill);
        }
        return bills;
    }

    @Override
    public Map<String, Object> selectBillDetail(FamilyToken family, Long billId)
    {
        if (billId == null)
        {
            throw new ServiceException("账单ID不能为空");
        }
        Bill bill = familyMapper.selectBillById(billId, family.getTenantId());
        if (bill == null)
        {
            throw new ServiceException("账单不存在");
        }
        checkElderOwnership(family, bill.getElderId());
        billService.fillPenalty(bill);
        List<BillItem> items = familyMapper.selectBillItems(billId, family.getTenantId());
        List<PaymentRecord> records = paymentRecordMapper.selectPaymentRecordsByBillIdAndTenant(billId, family.getTenantId());
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("bill", bill);
        result.put("items", items);
        result.put("records", records);
        return result;
    }

    @Override
    public List<HealthReport> selectReports(FamilyToken family, Long elderId)
    {
        checkElderOwnership(family, elderId);
        return familyMapper.selectReports(elderId, family.getTenantId());
    }

    @Override
    public HealthReport selectReportDetail(FamilyToken family, Long reportId)
    {
        if (reportId == null)
        {
            throw new ServiceException("报告ID不能为空");
        }
        HealthReport report = familyMapper.selectReportById(reportId, family.getTenantId());
        if (report == null)
        {
            throw new ServiceException("报告不存在");
        }
        checkElderOwnership(family, report.getElderId());
        return report;
    }

    @Override
    public Map<String, Object> selectDeposits(FamilyToken family, Long elderId)
    {
        FamilyElderVO elder = checkElderOwnership(family, elderId);
        BigDecimal balance = familyMapper.selectDepositBalance(elderId, family.getTenantId());
        List<Deposit> records = familyMapper.selectDeposits(elderId, family.getTenantId());
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("elderName", elder.getElderName());
        result.put("balance", balance == null ? BigDecimal.ZERO : balance);
        result.put("records", records);
        return result;
    }

    @Override
    public List<MessageLog> selectMessages(FamilyToken family)
    {
        return familyMapper.selectMessages(family.getPhone(), family.getTenantId());
    }

    @Override
    public Map<String, Object> selectServiceEvaluation(FamilyToken family, Long orderId)
    {
        ServiceOrder order = loadOwnedOrder(family, orderId);
        ServiceEvaluation evaluation = serviceEvaluationMapper.selectByOrderAndFamily(orderId,
                family.getFamilyId(), family.getTenantId());
        boolean finished = ORDER_STATUS_FINISHED.equals(order.getStatus());
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("evaluation", evaluation);
        result.put("canEvaluate", finished && evaluation == null);
        if (evaluation != null)
        {
            result.put("reason", "该服务已提交评价");
        }
        else if (!finished)
        {
            result.put("reason", "服务完成后才能评价");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServiceEvaluation submitServiceEvaluation(FamilyToken family, Long orderId, Integer rating, String content)
    {
        ServiceOrder order = loadOwnedOrder(family, orderId);
        if (!ORDER_STATUS_FINISHED.equals(order.getStatus()))
        {
            throw new ServiceException("服务完成后才能评价");
        }
        if (rating == null || rating.intValue() < 1 || rating.intValue() > 4)
        {
            throw new ServiceException("请选择服务满意度");
        }
        String trimmed = content == null ? null : content.trim();
        if (trimmed != null && trimmed.length() > MAX_EVALUATION_CONTENT)
        {
            throw new ServiceException("评价内容不能超过 " + MAX_EVALUATION_CONTENT + " 字");
        }
        ServiceEvaluation existing = serviceEvaluationMapper.selectByOrderAndFamily(orderId,
                family.getFamilyId(), family.getTenantId());
        if (existing != null)
        {
            throw new ServiceException("该服务已提交过评价");
        }

        ServiceEvaluation evaluation = new ServiceEvaluation();
        evaluation.setTenantId(family.getTenantId());
        evaluation.setOrderId(order.getOrderId());
        evaluation.setOrderNo(order.getOrderNo());
        evaluation.setElderId(order.getElderId());
        evaluation.setElderName(order.getElderName());
        evaluation.setFamilyId(family.getFamilyId());
        evaluation.setFamilyName(family.getFamilyName());
        evaluation.setPhone(family.getPhone());
        evaluation.setRating(rating);
        evaluation.setContent(trimmed == null || trimmed.length() == 0 ? null : trimmed);
        evaluation.setCreateBy(family.getPhone());
        try
        {
            serviceEvaluationMapper.insertEvaluation(evaluation);
        }
        catch (org.springframework.dao.DuplicateKeyException e)
        {
            throw new ServiceException("该服务已提交过评价");
        }
        return evaluation;
    }

    /**
     * 查询服务订单并校验其老人是否属于当前家属
     */
    private ServiceOrder loadOwnedOrder(FamilyToken family, Long orderId)
    {
        if (orderId == null)
        {
            throw new ServiceException("服务订单ID不能为空");
        }
        ServiceOrder order = familyMapper.selectServiceOrder(orderId, family.getTenantId());
        if (order == null)
        {
            throw new ServiceException("服务记录不存在");
        }
        checkElderOwnership(family, order.getElderId());
        return order;
    }

    @Override
    public List<Visit> selectVisits(FamilyToken family)
    {
        return familyMapper.selectVisitsByPhone(family.getPhone(), family.getTenantId());
    }

    @Override
    public Visit createVisit(FamilyToken family, Visit visit)
    {
        if (visit == null || visit.getElderId() == null)
        {
            throw new ServiceException("请选择探访老人");
        }
        FamilyElderVO elder = checkElderOwnership(family, visit.getElderId());
        if (visit.getVisitDate() == null)
        {
            throw new ServiceException("请选择探访日期");
        }
        if (visit.getVisitTime() == null || visit.getVisitTime().trim().length() == 0)
        {
            throw new ServiceException("请选择探访时间");
        }
        int peopleCount = visit.getPeopleCount() == null ? 1 : visit.getPeopleCount();
        if (peopleCount < 1 || peopleCount > MAX_PEOPLE_COUNT)
        {
            throw new ServiceException("探访人数需在 1-" + MAX_PEOPLE_COUNT + " 人之间");
        }
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        String visitDay = new SimpleDateFormat("yyyy-MM-dd").format(visit.getVisitDate());
        if (visitDay.compareTo(today) < 0)
        {
            throw new ServiceException("探访日期不能早于今天");
        }

        Visit record = new Visit();
        record.setVisitType("1");
        record.setVisitorName(family.getFamilyName());
        record.setVisitorPhone(family.getPhone());
        record.setElderId(elder.getElderId());
        record.setElderName(elder.getElderName());
        record.setVisitDate(visit.getVisitDate());
        record.setVisitTime(visit.getVisitTime().trim());
        record.setPeopleCount(peopleCount);
        record.setStatus("0");
        record.setVisitCode("V" + new SimpleDateFormat("yyMMdd").format(new java.util.Date())
                + String.format("%04d", (int) (Math.random() * 10000)));
        record.setRemark(visit.getRemark());
        record.setTenantId(family.getTenantId());
        record.setCreateBy(family.getPhone());
        visitMapper.insertVisit(record);
        return record;
    }

    @Override
    public int cancelVisit(FamilyToken family, Long visitId)
    {
        if (visitId == null)
        {
            throw new ServiceException("预约ID不能为空");
        }
        int rows = familyMapper.cancelVisit(visitId, family.getPhone(), family.getTenantId());
        if (rows == 0)
        {
            throw new ServiceException("预约不存在或当前状态无法取消");
        }
        return rows;
    }

    /**
     * 家属端在线缴费（模拟微信支付）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Bill payBill(FamilyToken family, Long billId, BigDecimal amount)
    {
        if (billId == null)
        {
            throw new ServiceException("账单ID不能为空");
        }
        Bill bill = familyMapper.selectBillById(billId, family.getTenantId());
        if (bill == null)
        {
            throw new ServiceException("账单不存在");
        }
        checkElderOwnership(family, bill.getElderId());
        if ("1".equals(bill.getPayStatus()))
        {
            throw new ServiceException("账单已缴清，无需重复缴费");
        }
        if ("2".equals(bill.getPayStatus()))
        {
            throw new ServiceException("账单已退款，不能缴费");
        }

        BigDecimal total = nvl(bill.getAmount());
        BigDecimal paid = nvl(bill.getPaidAmount());
        BigDecimal prepaid = nvl(bill.getPrepaid());
        BigDecimal payable = total.subtract(prepaid).subtract(paid);
        if (payable.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("账单已结清，无需缴费");
        }
        BigDecimal payAmount = amount == null ? payable : amount;
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("缴费金额必须大于0");
        }
        if (payAmount.compareTo(payable) > 0)
        {
            throw new ServiceException("缴费金额（" + payAmount + "）超过应付剩余（" + payable + "）");
        }

        BigDecimal newPaid = paid.add(payAmount);
        String payStatus = newPaid.compareTo(total) >= 0 ? "1" : "3";
        int rows = familyMapper.updateBillPay(billId, family.getTenantId(), newPaid, payStatus, paid,
                "家属-" + family.getPhone());
        if (rows == 0)
        {
            throw new ServiceException("账单已被其他操作更新，请刷新后重试");
        }

        PaymentRecord record = new PaymentRecord();
        record.setRecordNo(generateRecordNo());
        record.setBillId(bill.getBillId());
        record.setBillNo(bill.getBillNo());
        record.setElderId(bill.getElderId());
        record.setElderName(bill.getElderName());
        record.setPayAmount(payAmount);
        record.setPayMethod("0");
        record.setRemark("家属端在线缴费（演示环境模拟支付）");
        record.setOperatorName("家属-" + family.getFamilyName());
        record.setTenantId(family.getTenantId());
        record.setCreateBy(family.getPhone());
        paymentRecordMapper.insertPaymentRecord(record);

        Bill updated = familyMapper.selectBillById(billId, family.getTenantId());
        billService.fillPenalty(updated);
        return updated;
    }

    /** 收款单号：PAY + 时间戳 + 序列 + 随机数 */
    private String generateRecordNo()
    {
        return "PAY" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
                + String.format("%04d", RECORD_SEQ.incrementAndGet() % 10000)
                + String.format("%03d", (int) (Math.random() * 1000));
    }

    private static BigDecimal nvl(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 校验老人是否属于当前家属
     */
    private FamilyElderVO checkElderOwnership(FamilyToken family, Long elderId)
    {
        FamilyElderVO elder = familyMapper.selectElderDetail(family.getPhone(), family.getTenantId(), elderId);
        if (elder == null)
        {
            throw new ServiceException("无权查看该老人信息");
        }
        return elder;
    }

    // ====================== 公开接口（无需 token） ======================

    @Override
    public List<ServiceItem> selectPublicServiceItems()
    {
        ServiceItem query = new ServiceItem();
        query.setStatus("0");
        query.setTenantId(DEMO_TENANT_ID);
        List<ServiceItem> list = serviceItemService.selectServiceItemList(query);
        if (list == null)
        {
            return Collections.emptyList();
        }
        // 屏蔽单价以外的敏感字段
        for (ServiceItem item : list)
        {
            item.setTenantId(null);
            item.setDelFlag(null);
        }
        return list;
    }

    @Override
    public ServiceItem selectPublicServiceItemById(Long itemId)
    {
        if (itemId == null)
        {
            throw new ServiceException("服务项目ID不能为空");
        }
        ServiceItem item = serviceItemService.selectServiceItemById(itemId);
        if (item == null || !DEMO_TENANT_ID.equals(item.getTenantId()) || !"0".equals(item.getStatus()))
        {
            throw new ServiceException("服务项目不存在或暂未上架");
        }
        item.setTenantId(null);
        item.setDelFlag(null);
        return item;
    }

    @Override
    public List<Map<String, Object>> selectPublicRooms()
    {
        Room query = new Room();
        query.setStatus("0");
        query.setTenantId(DEMO_TENANT_ID);
        List<Room> rooms = roomService.selectRoomList(query);
        if (rooms == null || rooms.isEmpty())
        {
            return Collections.emptyList();
        }

        List<Long> roomIds = rooms.stream().map(Room::getRoomId).collect(Collectors.toList());
        List<Bed> beds = bedMapper.selectBedListByRoomIds(roomIds);
        Map<Long, List<Bed>> bedsByRoom = beds.stream()
                .collect(Collectors.groupingBy(Bed::getRoomId));

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Room room : rooms)
        {
            List<Bed> roomBeds = bedsByRoom.getOrDefault(room.getRoomId(), Collections.emptyList());
            int available = (int) roomBeds.stream().filter(b -> "0".equals(b.getStatus())).count();
            Map<String, Object> row = new HashMap<>();
            row.put("roomId", room.getRoomId());
            row.put("roomNo", room.getRoomNo());
            row.put("roomType", room.getRoomType());
            row.put("floorNo", room.getFloorNo());
            row.put("bedCount", room.getBedCount() == null ? 0 : room.getBedCount());
            row.put("availableBeds", available);
            row.put("status", room.getStatus());
            result.add(row);
        }
        return result;
    }

    @Override
    public Map<String, Object> selectPublicRoomDetail(Long roomId)
    {
        if (roomId == null)
        {
            throw new ServiceException("房型ID不能为空");
        }
        Room room = roomService.selectRoomById(roomId);
        if (room == null || !DEMO_TENANT_ID.equals(room.getTenantId()) || !"0".equals(room.getStatus()))
        {
            throw new ServiceException("房型不存在或暂未启用");
        }
        Bed bedQuery = new Bed();
        bedQuery.setRoomId(roomId);
        bedQuery.setTenantId(DEMO_TENANT_ID);
        List<Bed> beds = bedMapper.selectBedList(bedQuery);

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> roomInfo = new HashMap<>();
        roomInfo.put("roomId", room.getRoomId());
        roomInfo.put("roomNo", room.getRoomNo());
        roomInfo.put("roomType", room.getRoomType());
        roomInfo.put("floorNo", room.getFloorNo());
        roomInfo.put("bedCount", room.getBedCount());
        roomInfo.put("status", room.getStatus());
        result.put("room", roomInfo);

        List<Map<String, Object>> bedList = new java.util.ArrayList<>();
        for (Bed bed : beds)
        {
            Map<String, Object> b = new HashMap<>();
            b.put("bedId", bed.getBedId());
            b.put("bedNo", bed.getBedNo());
            b.put("status", bed.getStatus());
            b.put("elderName", maskElderName(bed.getElderName()));
            bedList.add(b);
        }
        result.put("beds", bedList);
        int available = (int) beds.stream().filter(b -> "0".equals(b.getStatus())).count();
        result.put("availableBeds", available);
        return result;
    }

    /** 公开页面老人姓名脱敏：仅保留姓氏，如「张*」 */
    private String maskElderName(String name)
    {
        if (name == null || name.isEmpty())
        {
            return name;
        }
        return name.length() == 1 ? "*" : name.charAt(0) + "*";
    }

    // ====================== 入住申请（需 token） ======================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Checkin createCheckin(FamilyToken family, Checkin checkin)
    {
        if (checkin == null)
        {
            throw new ServiceException("请填写入住申请信息");
        }
        if (checkin.getElderName() == null || checkin.getElderName().trim().length() == 0)
        {
            throw new ServiceException("请填写老人姓名");
        }
        if (checkin.getGender() == null)
        {
            throw new ServiceException("请选择老人性别");
        }
        if (checkin.getAge() == null || checkin.getAge() < 40 || checkin.getAge() > 120)
        {
            throw new ServiceException("请填写有效年龄");
        }
        if (checkin.getPhone() == null || !checkin.getPhone().matches("^1\\d{10}$"))
        {
            throw new ServiceException("请填写有效的联系电话");
        }

        checkin.setTenantId(family.getTenantId());
        checkin.setFamilyPhone(family.getPhone());
        checkin.setFamilyName(family.getFamilyName());
        if (checkin.getFamilyRelation() == null || checkin.getFamilyRelation().trim().length() == 0)
        {
            checkin.setFamilyRelation("家属");
        }
        checkin.setApplyStatus(CHECKIN_STATUS_APPLYING);
        checkin.setApplyNo(generateCheckinNo());
        checkin.setCreateBy(family.getPhone());
        checkinService.insertCheckin(checkin);
        return checkin;
    }

    @Override
    public List<Checkin> selectCheckins(FamilyToken family)
    {
        return familyMapper.selectCheckinsByFamily(family.getPhone(), family.getTenantId());
    }

    @Override
    public Checkin selectCheckinDetail(FamilyToken family, Long checkinId)
    {
        Checkin checkin = familyMapper.selectCheckinDetailByFamily(checkinId, family.getPhone(),
                family.getTenantId());
        if (checkin == null)
        {
            throw new ServiceException("入住申请不存在");
        }
        return checkin;
    }

    @Override
    public int cancelCheckin(FamilyToken family, Long checkinId)
    {
        Checkin checkin = familyMapper.selectCheckinDetailByFamily(checkinId, family.getPhone(),
                family.getTenantId());
        if (checkin == null)
        {
            throw new ServiceException("入住申请不存在或无权操作");
        }
        // 仅申请中(0) / 待评估(1) / 已驳回(9) 可撤销
        String status = checkin.getApplyStatus();
        if (!CHECKIN_STATUS_APPLYING.equals(status) && !"1".equals(status) && !CHECKIN_STATUS_REJECTED.equals(status))
        {
            throw new ServiceException("当前状态不允许撤销");
        }
        int rows = familyMapper.cancelCheckinByFamily(checkinId, family.getPhone(), family.getTenantId());
        if (rows == 0)
        {
            throw new ServiceException("撤销失败，请刷新后重试");
        }
        return rows;
    }

    @Override
    public int signCheckinContract(FamilyToken family, Long checkinId)
    {
        Checkin checkin = familyMapper.selectCheckinDetailByFamily(checkinId, family.getPhone(),
                family.getTenantId());
        if (checkin == null)
        {
            throw new ServiceException("入住申请不存在或无权操作");
        }
        if (!"4".equals(checkin.getApplyStatus()))
        {
            throw new ServiceException("当前状态不允许签约");
        }
        int rows = familyMapper.signCheckinByFamily(checkinId, family.getPhone(), family.getTenantId());
        if (rows == 0)
        {
            throw new ServiceException("签约失败，请刷新后重试");
        }
        return rows;
    }

    /** 入住申请单号：CK + 时间戳 + 序号 + 随机数 */
    private String generateCheckinNo()
    {
        return "CK" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
                + String.format("%04d", RECORD_SEQ.incrementAndGet() % 10000)
                + String.format("%03d", (int) (Math.random() * 1000));
    }

    // ====================== 绑定/解绑老人 ======================

    @Override
    public List<FamilyElderVO> matchElders(FamilyToken family, String elderName)
    {
        if (elderName == null || elderName.trim().isEmpty())
        {
            throw new ServiceException("请填写老人姓名");
        }
        return familyMapper.selectEldersForBinding(family.getTenantId(), elderName.trim());
    }

    @Override
    public FamilyMember bindElder(FamilyToken family, String elderName, String phone, String relation,
            Long elderId)
    {
        if (elderName == null || elderName.trim().isEmpty())
        {
            throw new ServiceException("请填写老人姓名");
        }
        String contactPhone = phone == null ? "" : phone.trim();
        if (!contactPhone.isEmpty() && !contactPhone.matches("^1\\d{10}$"))
        {
            throw new ServiceException("请填写正确的联系电话");
        }
        String name = elderName.trim();
        String rel = relation == null || relation.trim().isEmpty() ? "家属" : relation.trim();
        // 手机号仅作为联系方式记录，不参与老人档案匹配
        FamilyElderVO elder;
        if (elderId != null)
        {
            // 用户在匹配结果中明确选择了档案
            elder = familyMapper.selectElderForBindingById(family.getTenantId(), elderId);
            if (elder == null)
            {
                throw new ServiceException("所选老人档案不存在");
            }
            if (!name.equals(elder.getElderName()))
            {
                throw new ServiceException("所选老人与填写的姓名不一致，请重新匹配");
            }
        }
        else
        {
            List<FamilyElderVO> matches = familyMapper.selectEldersForBinding(family.getTenantId(), name);
            if (matches.isEmpty())
            {
                // 首次绑定：机构内没有该老人档案，自动创建一条新档案
                Elder newElder = new Elder();
                newElder.setElderName(name);
                newElder.setGender("0");
                newElder.setPhone(contactPhone);
                newElder.setStatus("2");
                newElder.setFamilyName(family.getFamilyName());
                newElder.setFamilyRelation(rel);
                newElder.setFamilyPhone(family.getPhone());
                newElder.setRemark("家属端首次绑定自动创建");
                newElder.setTenantId(family.getTenantId());
                newElder.setCreateBy("family:" + family.getPhone());
                elderMapper.insertElder(newElder);

                elder = new FamilyElderVO();
                elder.setElderId(newElder.getElderId());
                elder.setElderName(name);
            }
            else if (matches.size() > 1)
            {
                throw new ServiceException("机构内存在多位同名老人，请选择具体档案后再绑定");
            }
            else
            {
                elder = matches.get(0);
            }
        }

        FamilyMember existing = familyMapper.selectBindingByFamilyAndElder(family.getPhone(), elder.getElderId(),
                family.getTenantId());
        if (existing != null)
        {
            if ("0".equals(existing.getStatus()))
            {
                throw new ServiceException("已经绑定该老人");
            }
            // 解绑后重新绑定：恢复原记录，避免 uk_family 唯一键冲突
            familyMapper.reactivateBinding(existing.getFamilyId(), rel);
            existing.setStatus("0");
            existing.setRelation(rel);
            fillElderInfo(existing, elder);
            return existing;
        }

        FamilyMember m = new FamilyMember();
        m.setFamilyName(family.getFamilyName());
        m.setPhone(family.getPhone());
        m.setElderId(elder.getElderId());
        m.setRelation(rel);
        m.setStatus("0");
        m.setTenantId(family.getTenantId());
        m.setRemark(contactPhone.isEmpty() ? null : "联系电话：" + contactPhone);
        familyMapper.insertFamilyMember(m);
        fillElderInfo(m, elder);
        return m;
    }

    /** 绑定结果回填老人姓名与房号床位，便于家属端直接展示 */
    private void fillElderInfo(FamilyMember member, FamilyElderVO elder)
    {
        member.setElderName(elder.getElderName());
        member.setFloorNo(elder.getFloorNo());
        member.setRoomNo(elder.getRoomNo());
        member.setBedNo(elder.getBedNo());
    }

    @Override
    public List<FamilyMember> selectBindings(FamilyToken family)
    {
        return familyMapper.selectBindingsByFamily(family.getPhone(), family.getTenantId());
    }

    @Override
    public int unbindElder(FamilyToken family, Long elderId)
    {
        FamilyMember m = familyMapper.selectBindingByFamilyAndElder(family.getPhone(), elderId,
                family.getTenantId());
        if (m == null)
        {
            throw new ServiceException("未找到绑定关系");
        }
        if ("1".equals(m.getStatus()))
        {
            throw new ServiceException("已解绑");
        }
        return familyMapper.unbindByFamilyAndElder(family.getPhone(), elderId, family.getTenantId());
    }

    // ====================== 服务下单 ======================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServiceOrder createOrder(FamilyToken family, Long elderId, Long itemId, Date serviceTime,
            String remark)
    {
        FamilyElderVO elder = checkElderOwnership(family, elderId);
        ServiceItem item = serviceItemService.selectServiceItemById(itemId);
        if (item == null || !"0".equals(item.getStatus()))
        {
            throw new ServiceException("服务项目不存在或暂未上架");
        }

        ServiceOrder order = new ServiceOrder();
        order.setOrderNo("SO" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
                + String.format("%04d", RECORD_SEQ.incrementAndGet() % 10000));
        order.setElderId(elderId);
        order.setElderName(elder.getElderName());
        order.setItemId(itemId);
        order.setItemName(item.getItemName());
        order.setServiceTime(serviceTime == null ? new Date() : serviceTime);
        order.setAmount(item.getPrice() == null ? BigDecimal.ZERO : item.getPrice());
        order.setStatus("0");
        order.setTenantId(family.getTenantId());
        order.setRemark(remark);
        order.setCreateBy(family.getPhone());
        serviceOrderMapper.insertServiceOrder(order);
        return order;
    }

    @Override
    public int cancelOrder(FamilyToken family, Long orderId)
    {
        if (orderId == null) throw new ServiceException("订单ID不能为空");
        ServiceOrder order = serviceOrderMapper.selectServiceOrderById(orderId);
        if (order == null || !family.getTenantId().equals(order.getTenantId()))
        {
            throw new ServiceException("订单不存在或无权操作");
        }
        if (!"0".equals(order.getStatus()))
        {
            throw new ServiceException("订单已开始执行，无法取消");
        }
        order.setStatus("3");
        order.setUpdateBy(family.getPhone());
        return serviceOrderMapper.updateServiceOrder(order);
    }

    // ====================== 体征查询 ======================

    @Override
    public List<HealthRecord> selectRecentHealth(FamilyToken family, Long elderId, int days)
    {
        checkElderOwnership(family, elderId);
        int limit = Math.max(1, Math.min(90, days));
        return healthRecordMapper.selectRecentByElderId(elderId, limit);
    }
}
