import request from '@/utils/request'

// 查询发票申请列表
export function listInvoice(query) {
  return request({
    url: '/business/invoice/list',
    method: 'get',
    params: query
  })
}

// 查询发票申请详细
export function getInvoice(id) {
  return request({
    url: '/business/invoice/' + id,
    method: 'get'
  })
}

// 处理发票申请（status=1 已开票，status=2 驳回）
export function handleInvoice(data) {
  return request({
    url: '/business/invoice',
    method: 'put',
    data: data
  })
}

// 删除发票申请
export function delInvoice(ids) {
  return request({
    url: '/business/invoice/' + ids,
    method: 'delete'
  })
}
