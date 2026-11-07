import request from '@/utils/request'

// 查询体征记录列表
export function listHealthRecord(query) {
  return request({ url: '/business/health-record/list', method: 'get', params: query })
}

// 查询体征记录详情
export function getHealthRecord(recordId) {
  return request({ url: '/business/health-record/' + recordId, method: 'get' })
}

// 查询老人最近 N 条体征
export function getRecentHealth(elderId, limit = 30) {
  return request({ url: '/business/health-record/recent', method: 'get', params: { elderId, limit } })
}

// 新增体征记录
export function addHealthRecord(data) {
  return request({ url: '/business/health-record', method: 'post', data: data })
}

// 修改体征记录
export function updateHealthRecord(data) {
  return request({ url: '/business/health-record', method: 'put', data: data })
}

// 删除体征记录
export function delHealthRecord(recordIds) {
  return request({ url: '/business/health-record/' + recordIds, method: 'delete' })
}
