/** 家属登录信息 */
export interface FamilyInfo {
  familyId: number
  phone: string
  familyName: string
  relation: string
  tenantId: number
}

/** 老人档案（含在住信息） */
export interface ElderVO {
  elderId: number
  elderName: string
  gender: string
  age: number
  birthday?: string
  idCard?: string
  address?: string
  phone?: string
  careLevel: string
  checkinDate?: string
  status: string
  avatar?: string
  familyName?: string
  familyRelation?: string
  floorNo?: number
  roomNo?: string
  bedNo?: string
  nurseNames?: string
}

/** 服务记录 */
export interface ServiceOrder {
  orderId: number
  orderNo: string
  elderId: number
  elderName: string
  itemName: string
  nurseName: string
  serviceTime: string
  amount: number
  status: string
  remark?: string
  /** 关联护理任务 */
  taskId?: number
  taskStatus?: string
  taskFeedback?: string
}

/** 服务评价 */
export interface ServiceEvaluation {
  evalId: number
  orderId: number
  orderNo?: string
  elderId: number
  elderName?: string
  familyId: number
  familyName?: string
  /** 满意度（1需改进 2一般 3满意 4非常满意） */
  rating: number
  content?: string
  createTime?: string
}

/** 服务评价查询结果 */
export interface ServiceEvaluationInfo {
  evaluation: ServiceEvaluation | null
  canEvaluate: boolean
  reason?: string
}

/** 押金记录 */
export interface DepositRecord {
  depositId: number
  elderId: number
  elderName: string
  depositType: string
  amount: number
  payMethod?: string
  remark?: string
  operatorName?: string
  createTime?: string
}

/** 押金余额与流水 */
export interface DepositInfo {
  elderName: string
  balance: number
  records: DepositRecord[]
}

/** 消息通知 */
export interface MessageLog {
  logId: number
  elderId?: number
  elderName?: string
  phone?: string
  msgType: string
  title: string
  content: string
  sendStatus: string
  errorMsg?: string
  createTime?: string
}

/** 账单 */
export interface Bill {
  billId: number
  billNo: string
  elderId: number
  elderName: string
  billMonth: string
  periodStart?: string
  periodEnd?: string
  amount: number
  deposit: number
  prepaid: number
  paidAmount: number
  payStatus: string
  payTime?: string
  payMethod?: string
  billType: string
  remark?: string
  /** 滞纳金（逾期未缴金额按日费率累计） */
  penaltyAmount?: number
  /** 逾期天数（超过宽限期后的天数） */
  overdueDays?: number
}

/** 收款记录 */
export interface PaymentRecord {
  recordId: number
  recordNo: string
  billId: number
  billNo: string
  elderId: number
  elderName: string
  payAmount: number
  payMethod?: string
  operatorName?: string
  remark?: string
  createTime?: string
}

/** 账单详情（含明细与收款记录） */
export interface BillDetail {
  bill: Bill
  items: BillItem[]
  records: PaymentRecord[]
}

/** 账单明细 */
export interface BillItem {
  itemId: number
  billId: number
  itemType: string
  feeName: string
  serviceContent: string
  amount: number
}

/** 健康报告 */
export interface HealthReport {
  reportId: number
  elderId: number
  elderName: string
  reportType: string
  reportTitle: string
  periodStart?: string
  periodEnd?: string
  score: number
  riskLevel: string
  summary?: string
  content?: string
  modelSource?: string
  status: string
  createTime?: string
}

/** 探访预约 */
export interface Visit {
  visitId: number
  visitType: string
  visitorName: string
  visitorPhone: string
  elderId: number
  elderName: string
  visitDate: string
  visitTime: string
  peopleCount: number
  status: string
  /** 探访预约码 */
  visitCode?: string
  /** 到访核销时间 */
  checkinTime?: string
  remark?: string
  createTime?: string
}

/** 登录结果 */
export interface LoginResult {
  token: string
  family: FamilyInfo
  elders: ElderVO[]
}

// ====================== 公开浏览（无需登录） ======================

/** 公开-服务项目 */
export interface PublicServiceItem {
  itemId: number
  itemName: string
  price: number
  unit?: string
  image?: string
  description?: string
}

/** 公开-房型列表条目 */
export interface RoomView {
  roomId: number
  roomNo: string
  roomType: string
  floorNo: number
  bedCount: number
  availableBeds: number
  status?: string
}

/** 公开-床位状态（房型详情内） */
export interface BedView {
  bedId: number
  bedNo: string
  /** 0空闲 1已入住 2外出 3维修 */
  status: string
  elderName?: string
}

/** 公开-房型详情 */
export interface RoomDetail {
  /** 房间信息（详情接口不返回 availableBeds，空床数取外层的 availableBeds） */
  room: Omit<RoomView, 'availableBeds'>
  beds: BedView[]
  availableBeds: number
}

// ====================== 入住申请（需登录） ======================

/** 入住申请（家属视角） */
export interface CheckinApply {
  checkinId: number
  applyNo: string
  elderName: string
  gender: string
  age: number
  phone: string
  address?: string
  selfCare?: string
  mentalState?: string
  disease?: string
  /** 评估结论 / 建议护理等级 / 评估人 / 评估时间 */
  assessmentResult?: string
  suggestCareLevel?: string
  assessor?: string
  assessTime?: string
  /** 护理等级 / 费用标准 / 入住日期 */
  careLevel?: string
  /** 合同信息 */
  contractNo?: string
  contractStart?: string
  contractEnd?: string
  contractFile?: string
  signDate?: string
  /** 首期缴费状态 */
  payStatus?: string
  familyName?: string
  familyRelation?: string
  familyPhone?: string
  /** 0申请中 1待评估 2待审核 3待配置 4待签约 5待缴费 6已入住 9已驳回/撤销 */
  applyStatus: string
  auditResult?: string
  auditOpinion?: string
  roomId?: number
  bedId?: number
  feeStandard?: number
  checkinDate?: string
  createTime?: string
}

// ====================== 绑定/解绑 ======================

/** 家属绑定关系 */
export interface FamilyBinding {
  familyId: number
  familyName?: string
  phone?: string
  relation: string
  elderId: number
  /** 0 正常 1 已解绑 */
  status: string
  tenantId?: number
  elderName?: string
  floorNo?: number
  roomNo?: string
  bedNo?: string
  createTime?: string
}

// ====================== 体征查询 ======================

/** 老人体征记录 */
export interface HealthRecord {
  recordId: number
  elderId: number
  elderName?: string
  recordDate: string
  recordTime?: string
  bloodPressureHigh?: number   // 收缩压
  bloodPressureLow?: number    // 舒张压
  heartRate?: number           // 心率
  bodyTemp?: number            // 体温
  bloodSugar?: number          // 血糖
  bloodOxygen?: number         // 血氧
  weight?: number              // 体重
  steps?: number               // 步数
  sleepHours?: number          // 睡眠
  isAbnormal?: string          // 0正常 1异常
  abnormalItems?: string
}

// ====================== 日常照护 ======================

/** 请假外出申请 */
export interface LeaveRecord {
  leaveId: number
  elderId: number
  elderName?: string
  /** 0请假 1外出就医 2回家居住 */
  leaveType: string
  startTime: string
  endTime: string
  reason?: string
  contactPhone?: string
  /** 0待审批 1已通过 2已驳回 3已销假 4已撤销 */
  status: string
  auditOpinion?: string
  auditTime?: string
  backTime?: string
  createTime?: string
}

/** 用药提醒 */
export interface Medication {
  medicationId: number
  elderId: number
  elderName?: string
  medicineName: string
  dosage?: string
  frequency?: string
  timePoints?: string
  startDate?: string
  endDate?: string
  /** 0进行中 1已停用 */
  status: string
  remark?: string
}

/** 护理日志 */
export interface CareLog {
  logId: number
  elderId: number
  elderName?: string
  logDate: string
  /** 0日常照护 1饮食 2清洁 3康复 4心理关怀 */
  careType: string
  content?: string
  nurseName?: string
  createTime?: string
}

// ====================== 家属服务 ======================

/** 投诉建议 */
export interface FeedbackRecord {
  feedbackId: number
  elderId?: number
  elderName?: string
  familyPhone?: string
  familyName?: string
  /** 0投诉 1建议 2表扬 */
  feedbackType: string
  content: string
  /** 0待处理 1处理中 2已回复 */
  status: string
  reply?: string
  replyTime?: string
  createTime?: string
}

/** 发票申请 */
export interface InvoiceApply {
  invoiceId: number
  billId?: number
  billNo?: string
  elderId?: number
  elderName?: string
  title: string
  taxNo?: string
  amount?: number
  email?: string
  /** 0待开票 1已开票 2已驳回 */
  status: string
  invoiceNo?: string
  remark?: string
  createTime?: string
}

// ====================== AI 智能客服 ======================

/** 聊天会话 */
export interface ChatSession {
  sessionId: number
  elderId?: number
  title?: string
  createTime?: string
}

/** 聊天消息 */
export interface ChatMessage {
  msgId: number
  sessionId: number
  /** user/assistant */
  role: string
  content: string
  createTime?: string
}

/** 发送消息结果 */
export interface ChatSendResult {
  sessionId: number
  reply: string
}
