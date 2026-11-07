import request from '@/utils/request'
import { download } from '@/utils/request'

// 查询账单列表
export function listBill(query) {
  return request({
    url: '/business/bill/list',
    method: 'get',
    params: query
  })
}

// 查询账单详细
export function getBill(id) {
  return request({
    url: '/business/bill/' + id,
    method: 'get'
  })
}

// 生成月度账单
export function generateBill(data) {
  return request({
    url: '/business/bill/generate',
    method: 'post',
    data: data
  })
}

// 账单缴费（支持部分收款，传 payAmount）
export function payBill(data) {
  return request({
    url: '/business/bill/pay',
    method: 'put',
    data: data
  })
}

// 账单退款
export function refundBill(data) {
  return request({
    url: '/business/bill/refund',
    method: 'post',
    data: data
  })
}

// 删除账单
export function delBill(ids) {
  return request({
    url: '/business/bill/' + ids,
    method: 'delete'
  })
}

// 查询指定账单的收款记录
export function listPaymentRecords(billId) {
  return request({
    url: '/business/bill/record/' + billId,
    method: 'get'
  })
}

// 手动重算逾期账单滞纳金
export function refreshPenalty() {
  return request({
    url: '/business/bill/penalty/refresh',
    method: 'post'
  })
}

// 向账单家属发送微信提醒（Mock 模式只落库发送记录）
export function notifyBill(billId, msgType) {
  return request({
    url: '/business/bill/notify/' + billId,
    method: 'post',
    params: { msgType: msgType || '1' }
  })
}

// 查询逾期账单
export function getOverdueBills() {
  return request({
    url: '/business/bill/overdue',
    method: 'get'
  })
}

// 查询发票打印数据（账单 + 明细 + 收款记录）
export function getInvoiceData(billId) {
  return request({
    url: '/business/bill/invoice/' + billId,
    method: 'get'
  })
}

// 下载后端生成的发票 PDF（带 token 的鉴权下载）
export function downloadInvoicePdf(billId) {
  // filename 会由后端 Content-Disposition 指定，这里传 'invoice.pdf' 兜底
  return download('/business/bill/invoice/' + billId + '/pdf', {}, '发票.pdf')
}
