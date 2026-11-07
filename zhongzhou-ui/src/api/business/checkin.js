import request from '@/utils/request'

// 查询入住申请列表
export function listCheckin(query) {
  return request({
    url: '/business/checkin/list',
    method: 'get',
    params: query
  })
}

// 查询入住申请详细
export function getCheckin(id) {
  return request({
    url: '/business/checkin/' + id,
    method: 'get'
  })
}

// 新增入住申请
export function addCheckin(data) {
  return request({
    url: '/business/checkin',
    method: 'post',
    data: data
  })
}

// 修改入住申请
export function updateCheckin(data) {
  return request({
    url: '/business/checkin',
    method: 'put',
    data: data
  })
}

// 提交入住向导步骤（1-6）
export function stepCheckin(id, step, data) {
  return request({
    url: '/business/checkin/step/' + id + '/' + step,
    method: 'put',
    data: data
  })
}

// 入住审核
export function auditCheckin(data) {
  return request({
    url: '/business/checkin/audit',
    method: 'put',
    data: data
  })
}

// 完成入住
export function completeCheckin(id) {
  return request({
    url: '/business/checkin/' + id + '/complete',
    method: 'post'
  })
}

// 驳回后重新提交
export function resubmitCheckin(id) {
  return request({
    url: '/business/checkin/' + id + '/resubmit',
    method: 'post'
  })
}

// 删除入住申请
export function delCheckin(ids) {
  return request({
    url: '/business/checkin/' + ids,
    method: 'delete'
  })
}
