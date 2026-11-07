import request from '@/utils/request'

// 查询健康报告列表
export function listReport(query) {
  return request({
    url: '/business/report/list',
    method: 'get',
    params: query
  })
}

// 查询健康报告详细
export function getReport(reportId) {
  return request({
    url: '/business/report/' + reportId,
    method: 'get'
  })
}

// 生成健康报告（AI 生成耗时较长，单独放宽超时）
export function generateReport(data) {
  return request({
    url: '/business/report/generate',
    method: 'post',
    data: data,
    timeout: 120000
  })
}

// 重新生成健康报告
export function regenerateReport(reportId) {
  return request({
    url: '/business/report/regenerate/' + reportId,
    method: 'post',
    timeout: 120000
  })
}

// 删除健康报告
export function delReport(reportIds) {
  return request({
    url: '/business/report/' + reportIds,
    method: 'delete'
  })
}

// 导出健康报告（Word）
export function exportReport(reportId) {
  return request({
    url: '/business/report/export/' + reportId,
    method: 'get',
    responseType: 'blob'
  })
}
