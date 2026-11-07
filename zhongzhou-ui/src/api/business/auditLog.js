import request from '@/utils/request'

// 查询业务审计日志列表
export function listAuditLog(query) {
  return request({
    url: '/business/auditLog/list',
    method: 'get',
    params: query
  })
}
