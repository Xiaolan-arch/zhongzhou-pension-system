import request from '@/utils/request'

// 查询请假外出列表
export function listLeave(query) {
  return request({
    url: '/business/leave/list',
    method: 'get',
    params: query
  })
}

// 查询请假外出详细
export function getLeave(id) {
  return request({
    url: '/business/leave/' + id,
    method: 'get'
  })
}

// 新增请假外出
export function addLeave(data) {
  return request({
    url: '/business/leave',
    method: 'post',
    data: data
  })
}

// 修改请假外出
export function updateLeave(data) {
  return request({
    url: '/business/leave',
    method: 'put',
    data: data
  })
}

// 删除请假外出
export function delLeave(ids) {
  return request({
    url: '/business/leave/' + ids,
    method: 'delete'
  })
}

// 审批请假（status=1 通过，status=2 驳回）
export function auditLeave(id, status, auditOpinion) {
  return request({
    url: '/business/leave/audit/' + id,
    method: 'put',
    params: { status, auditOpinion }
  })
}

// 销假
export function backLeave(id) {
  return request({
    url: '/business/leave/back/' + id,
    method: 'put'
  })
}
