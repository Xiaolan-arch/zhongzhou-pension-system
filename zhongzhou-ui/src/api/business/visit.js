import request from '@/utils/request'

// 查询来访列表
export function listVisit(query) {
  return request({
    url: '/business/visit/list',
    method: 'get',
    params: query
  })
}

// 查询来访详细
export function getVisit(id) {
  return request({
    url: '/business/visit/' + id,
    method: 'get'
  })
}

// 新增来访
export function addVisit(data) {
  return request({
    url: '/business/visit',
    method: 'post',
    data: data
  })
}

// 修改来访
export function updateVisit(data) {
  return request({
    url: '/business/visit',
    method: 'put',
    data: data
  })
}

// 删除来访
export function delVisit(ids) {
  return request({
    url: '/business/visit/' + ids,
    method: 'delete'
  })
}

// 确认来访
export function confirmVisit(id) {
  return request({
    url: '/business/visit/confirm/' + id,
    method: 'put'
  })
}

// 取消来访
export function cancelVisit(id) {
  return request({
    url: '/business/visit/cancel/' + id,
    method: 'put'
  })
}

// 到访核销
export function checkinVisit(id) {
  return request({
    url: '/business/visit/checkin/' + id,
    method: 'put'
  })
}
