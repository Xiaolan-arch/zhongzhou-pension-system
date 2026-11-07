import request from '@/utils/request'

// 查询用药提醒列表
export function listMedication(query) {
  return request({
    url: '/business/medication/list',
    method: 'get',
    params: query
  })
}

// 查询用药提醒详细
export function getMedication(id) {
  return request({
    url: '/business/medication/' + id,
    method: 'get'
  })
}

// 新增用药提醒
export function addMedication(data) {
  return request({
    url: '/business/medication',
    method: 'post',
    data: data
  })
}

// 修改用药提醒
export function updateMedication(data) {
  return request({
    url: '/business/medication',
    method: 'put',
    data: data
  })
}

// 删除用药提醒
export function delMedication(ids) {
  return request({
    url: '/business/medication/' + ids,
    method: 'delete'
  })
}
