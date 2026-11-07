import request from '@/utils/request'

// 查询退住申请列表
export function listCheckout(query) {
  return request({
    url: '/business/checkout/list',
    method: 'get',
    params: query
  })
}

// 查询退住申请详细
export function getCheckout(id) {
  return request({
    url: '/business/checkout/' + id,
    method: 'get'
  })
}

// 新增退住申请
export function addCheckout(data) {
  return request({
    url: '/business/checkout',
    method: 'post',
    data: data
  })
}

// 修改退住申请
export function updateCheckout(data) {
  return request({
    url: '/business/checkout',
    method: 'put',
    data: data
  })
}

// 提交退住向导步骤（1-6）
export function stepCheckout(id, step, data) {
  return request({
    url: '/business/checkout/step/' + id + '/' + step,
    method: 'put',
    data: data
  })
}

// 完成退住
export function completeCheckout(id, data) {
  return request({
    url: '/business/checkout/' + id + '/complete',
    method: 'post',
    data: data
  })
}

// 删除退住申请
export function delCheckout(ids) {
  return request({
    url: '/business/checkout/' + ids,
    method: 'delete'
  })
}
