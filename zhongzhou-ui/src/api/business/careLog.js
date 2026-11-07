import request from '@/utils/request'

// 查询护理日志列表
export function listCareLog(query) {
  return request({
    url: '/business/careLog/list',
    method: 'get',
    params: query
  })
}

// 查询护理日志详细
export function getCareLog(id) {
  return request({
    url: '/business/careLog/' + id,
    method: 'get'
  })
}

// 新增护理日志
export function addCareLog(data) {
  return request({
    url: '/business/careLog',
    method: 'post',
    data: data
  })
}

// 修改护理日志
export function updateCareLog(data) {
  return request({
    url: '/business/careLog',
    method: 'put',
    data: data
  })
}

// 删除护理日志
export function delCareLog(ids) {
  return request({
    url: '/business/careLog/' + ids,
    method: 'delete'
  })
}
