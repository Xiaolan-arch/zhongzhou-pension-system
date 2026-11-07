/** 金额格式化 */
export function formatMoney(value?: number | string | null): string {
  const num = Number(value || 0)
  return `¥${num.toFixed(2)}`
}

/** 日期截取 yyyy-MM-dd */
export function formatDate(value?: string | null): string {
  if (!value) return '--'
  return value.substring(0, 10)
}

/** 日期时间截取 yyyy-MM-dd HH:mm */
export function formatDateTime(value?: string | null): string {
  if (!value) return '--'
  return value.substring(0, 16).replace('T', ' ')
}

/** 护理等级文案 */
export function careLevelText(value?: string): string {
  const map: Record<string, string> = { '1': '完全自理', '2': '半自理', '3': '重度失能', '4': '特护' }
  return map[value || ''] || '未评估'
}

/** 老人状态文案 */
export function elderStatusText(value?: string): string {
  const map: Record<string, string> = { '0': '在住', '1': '已退住', '2': '待入住' }
  return map[value || ''] || '未知'
}

/** 服务订单状态文案 */
export function orderStatusText(value?: string): string {
  const map: Record<string, string> = { '0': '待执行', '1': '已执行', '2': '已完成', '3': '已取消' }
  return map[value || ''] || '未知'
}

/** 账单支付状态文案 */
export function billStatusText(value?: string): string {
  const map: Record<string, string> = { '0': '待支付', '1': '已支付', '2': '已退款', '3': '部分已付' }
  return map[value || ''] || '未知'
}

/** 账单类型文案 */
export function billTypeText(value?: string): string {
  const map: Record<string, string> = { '0': '首期账单', '1': '月度账单', '2': '退住结算' }
  return map[value || ''] || '账单'
}

/** 支付方式文案 */
export function payMethodText(value?: string): string {
  const map: Record<string, string> = { '0': '微信', '1': '支付宝', '2': '现金', '3': '银行卡', '4': '其他' }
  return map[value || ''] || '其他'
}

/** 押金类型文案 */
export function depositTypeText(value?: string): string {
  return value === '2' ? '退押金' : '收押金'
}

/** 消息类型文案 */
export function msgTypeText(value?: string): string {
  const map: Record<string, string> = { '1': '账单到期', '2': '逾期提醒', '3': '缴费成功' }
  return map[value || ''] || '机构通知'
}

/** 探访状态文案 */
export function visitStatusText(value?: string): string {
  const map: Record<string, string> = { '0': '待确认', '1': '已确认', '2': '已完成', '3': '已取消' }
  return map[value || ''] || '未知'
}

/** 报告类型文案 */
export function reportTypeText(value?: string): string {
  const map: Record<string, string> = { '0': '综合照护报告', '1': '健康评估报告', '2': '月度总结报告' }
  return map[value || ''] || '健康报告'
}

/** 风险等级文案 */
export function riskLevelText(value?: string): string {
  const map: Record<string, string> = { '0': '状态良好', '1': '需要关注', '2': '重点预警' }
  return map[value || ''] || '状态良好'
}

/** 性别文案 */
export function genderText(value?: string): string {
  return value === '1' ? '女' : '男'
}

/** 截取姓名用于头像占位 */
export function nameInitial(name?: string): string {
  return (name || '长').substring(0, 1)
}

/** 请假类型文案 */
export function leaveTypeText(value?: string): string {
  const map: Record<string, string> = { '0': '请假', '1': '外出就医', '2': '回家居住' }
  return map[value || ''] || '请假'
}

/** 请假状态文案 */
export function leaveStatusText(value?: string): string {
  const map: Record<string, string> = {
    '0': '待审批',
    '1': '已通过',
    '2': '已驳回',
    '3': '已销假',
    '4': '已撤销'
  }
  return map[value || ''] || '未知'
}

/** 护理类型文案 */
export function careTypeText(value?: string): string {
  const map: Record<string, string> = {
    '0': '日常照护',
    '1': '饮食',
    '2': '清洁',
    '3': '康复',
    '4': '心理关怀'
  }
  return map[value || ''] || '日常照护'
}

/** 反馈类型文案 */
export function feedbackTypeText(value?: string): string {
  const map: Record<string, string> = { '0': '投诉', '1': '建议', '2': '表扬' }
  return map[value || ''] || '建议'
}

/** 反馈状态文案 */
export function feedbackStatusText(value?: string): string {
  const map: Record<string, string> = { '0': '待处理', '1': '处理中', '2': '已回复' }
  return map[value || ''] || '待处理'
}

/** 发票状态文案 */
export function invoiceStatusText(value?: string): string {
  const map: Record<string, string> = { '0': '待开票', '1': '已开票', '2': '已驳回' }
  return map[value || ''] || '待开票'
}
