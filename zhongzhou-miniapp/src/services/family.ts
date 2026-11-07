import { request } from './request'
import type {
  Bill,
  BillDetail,
  CareLog,
  ChatMessage,
  ChatSendResult,
  ChatSession,
  CheckinApply,
  DepositInfo,
  ElderVO,
  FamilyBinding,
  FeedbackRecord,
  HealthRecord,
  HealthReport,
  InvoiceApply,
  LeaveRecord,
  LoginResult,
  Medication,
  MessageLog,
  PublicServiceItem,
  RoomDetail,
  RoomView,
  ServiceEvaluation,
  ServiceEvaluationInfo,
  ServiceOrder,
  Visit
} from '../types'

/** 发送验证码（演示环境返回验证码） */
export function sendCode(phone: string) {
  return request<{ demoCode: string; expireSeconds: number }>('/family/auth/sendCode', {
    method: 'POST',
    data: { phone },
    skipAuthRedirect: true
  })
}

/** 手机号 + 验证码登录（wxCode 为小程序 wx.login 返回的 code，用于绑定 openid） */
export function login(phone: string, code: string, wxCode?: string) {
  return request<LoginResult>('/family/auth/login', {
    method: 'POST',
    data: { phone, code, wxCode },
    skipAuthRedirect: true
  })
}

/** 退出登录 */
export function logout() {
  return request<void>('/family/auth/logout', { method: 'POST', skipAuthRedirect: true })
}

/** 绑定的老人列表 */
export function getElders() {
  return request<ElderVO[]>('/family/elders')
}

/** 老人档案详情 */
export function getElder(elderId: number) {
  return request<ElderVO>(`/family/elders/${elderId}`)
}

/** 服务记录 */
export function getServices(elderId: number) {
  return request<ServiceOrder[]>(`/family/services?elderId=${elderId}`)
}

/** 查询服务评价 */
export function getServiceEvaluation(orderId: number) {
  return request<ServiceEvaluationInfo>(`/family/services/${orderId}/evaluation`)
}

/** 提交服务评价（同一订单同一家属仅可提交一次） */
export function submitServiceEvaluation(orderId: number, data: { rating: number; content?: string }) {
  return request<ServiceEvaluation>(`/family/services/${orderId}/evaluation`, { method: 'POST', data })
}

/** 账单列表 */
export function getBills(elderId: number) {
  return request<Bill[]>(`/family/bills?elderId=${elderId}`)
}

/** 账单详情（含费用明细与收款记录） */
export function getBillDetail(billId: number) {
  return request<BillDetail>(`/family/bills/${billId}`)
}

/** 在线缴费（演示环境为模拟支付，amount 为空表示缴清剩余应付） */
export function payBill(billId: number, amount?: number) {
  return request<Bill>(`/family/bills/${billId}/pay`, {
    method: 'POST',
    data: amount === undefined ? {} : { amount }
  })
}

/** 押金余额与流水 */
export function getDeposits(elderId: number) {
  return request<DepositInfo>(`/family/deposits?elderId=${elderId}`)
}

/** 消息通知列表 */
export function getMessages() {
  return request<MessageLog[]>('/family/messages')
}

/** 健康报告列表 */
export function getReports(elderId: number) {
  return request<HealthReport[]>(`/family/reports?elderId=${elderId}`)
}

/** 健康报告详情 */
export function getReportDetail(reportId: number) {
  return request<HealthReport>(`/family/reports/${reportId}`)
}

/** 探访预约列表 */
export function getVisits() {
  return request<Visit[]>('/family/visits')
}

/** 提交探访预约 */
export function createVisit(data: {
  elderId: number
  visitDate: string
  visitTime: string
  peopleCount: number
  remark?: string
}) {
  return request<Visit>('/family/visits', { method: 'POST', data })
}

/** 取消探访预约 */
export function cancelVisit(visitId: number) {
  return request<void>(`/family/visits/${visitId}/cancel`, { method: 'PUT' })
}

// ====================== 公开接口（无需登录） ======================

/** 公开-服务项目列表 */
export function getPublicServiceItems() {
  return request<PublicServiceItem[]>('/family/public/service-items', { skipAuthRedirect: true })
}

/** 公开-服务项目详情 */
export function getPublicServiceItemDetail(itemId: number) {
  return request<PublicServiceItem>(`/family/public/service-items/${itemId}`, { skipAuthRedirect: true })
}

/** 公开-房型列表（含空床数聚合） */
export function getPublicRooms() {
  return request<RoomView[]>('/family/public/rooms', { skipAuthRedirect: true })
}

/** 公开-房型详情（含床位状态） */
export function getPublicRoomDetail(roomId: number) {
  return request<RoomDetail>(`/family/public/rooms/${roomId}`, { skipAuthRedirect: true })
}

// ====================== 入住申请（需登录） ======================

/** 提交入住申请 */
export function createCheckin(data: {
  elderName: string
  gender: string
  age: number
  phone: string
  address?: string
  selfCare?: string
  mentalState?: string
  disease?: string
  familyRelation?: string
}) {
  return request<CheckinApply>('/family/checkins', { method: 'POST', data })
}

/** 我的入住申请列表 */
export function getCheckins() {
  return request<CheckinApply[]>('/family/checkins')
}

/** 入住申请详情 */
export function getCheckinDetail(checkinId: number) {
  return request<CheckinApply>(`/family/checkins/${checkinId}`)
}

/** 撤销入住申请 */
export function cancelCheckin(checkinId: number) {
  return request<void>(`/family/checkins/${checkinId}/cancel`, { method: 'PUT' })
}

/** 在线确认入住合同签约 */
export function signCheckin(checkinId: number) {
  return request<void>(`/family/checkins/${checkinId}/sign`, { method: 'POST' })
}

// ====================== 绑定/解绑 ======================

/** 绑定前按姓名匹配老人档案（0 条=新档案，1 条=确认关联，多条=同名选择） */
export function matchElders(elderName: string) {
  return request<ElderVO[]>(`/family/bindings/match?elderName=${encodeURIComponent(elderName)}`)
}

/** 发起绑定老人（按姓名匹配档案，手机号仅作联系方式；elderId 为同名档案中选择的档案） */
export function bindElder(elderName: string, phone: string, relation = '家属', elderId?: number) {
  return request<FamilyBinding>('/family/bindings', {
    method: 'POST',
    data: elderId ? { elderName, phone, relation, elderId } : { elderName, phone, relation }
  })
}

/** 我的绑定列表 */
export function getBindings() {
  return request<FamilyBinding[]>('/family/bindings')
}

/** 解绑 */
export function unbindElder(elderId: number) {
  return request<void>(`/family/bindings/${elderId}/unbind`, { method: 'PUT' })
}

// ====================== 服务下单 ======================

/** 家属端预约下单 */
export function createOrder(data: { elderId: number; itemId: number; serviceTime?: string; remark?: string }) {
  return request<ServiceOrder>('/family/orders', { method: 'POST', data })
}

/** 取消未开始订单 */
export function cancelFamilyOrder(orderId: number) {
  return request<void>(`/family/orders/${orderId}/cancel`, { method: 'PUT' })
}

// ====================== 体征查询 ======================

/** 最近 N 天体征 */
export function getRecentHealth(elderId: number, days = 7) {
  return request<HealthRecord[]>('/family/health-records/recent', { data: { elderId, days }, method: 'GET' })
}

// ====================== 请假外出 ======================

/** 请假外出记录 */
export function getLeaves(elderId: number) {
  return request<LeaveRecord[]>(`/family/leaves?elderId=${elderId}`)
}

/** 提交请假/外出申请 */
export function createLeave(data: {
  elderId: number
  leaveType: string
  startTime: string
  endTime: string
  reason: string
  contactPhone?: string
}) {
  return request<LeaveRecord>('/family/leaves', { method: 'POST', data })
}

/** 撤销待审批的请假申请 */
export function cancelLeave(leaveId: number) {
  return request<void>(`/family/leaves/${leaveId}/cancel`, { method: 'PUT' })
}

// ====================== 用药提醒 / 护理日志 ======================

/** 用药提醒列表 */
export function getMedications(elderId: number) {
  return request<Medication[]>(`/family/medications?elderId=${elderId}`)
}

/** 护理日志列表 */
export function getCareLogs(elderId: number) {
  return request<CareLog[]>(`/family/care-logs?elderId=${elderId}`)
}

// ====================== 投诉建议 ======================

/** 我的投诉建议 */
export function getFeedbacks() {
  return request<FeedbackRecord[]>('/family/feedback')
}

/** 提交投诉建议 */
export function createFeedback(data: {
  elderId?: number
  feedbackType: string
  content: string
}) {
  return request<FeedbackRecord>('/family/feedback', { method: 'POST', data })
}

// ====================== 发票申请 ======================

/** 我的开票申请 */
export function getInvoices(elderId: number) {
  return request<InvoiceApply[]>(`/family/invoices?elderId=${elderId}`)
}

/** 提交开票申请 */
export function createInvoice(data: {
  billId: number
  title: string
  taxNo?: string
  email?: string
}) {
  return request<InvoiceApply>('/family/invoices', { method: 'POST', data })
}

// ====================== AI 智能客服 ======================

/** 聊天会话列表 */
export function getChatSessions() {
  return request<ChatSession[]>('/family/chat/sessions')
}

/** 会话消息历史 */
export function getChatMessages(sessionId: number) {
  return request<ChatMessage[]>(`/family/chat/sessions/${sessionId}/messages`)
}

/** 发送消息并获取 AI 回复（sessionId 为空时自动新建会话） */
export function sendChat(data: { sessionId?: number; elderId?: number; content: string }) {
  return request<ChatSendResult>('/family/chat/send', { method: 'POST', data })
}
