import request from '@/utils/request'

// 查询护理员列表
export function listNurse(query) {
  return request({
    url: '/business/nurse/list',
    method: 'get',
    params: query
  })
}

// 单床护理员分配
export function assignNurse(data) {
  return request({
    url: '/business/nurse/assign',
    method: 'put',
    data: data
  })
}

// 整层批量分配护理员
export function batchAssignNurse(data) {
  return request({
    url: '/business/nurse/batchAssign',
    method: 'put',
    data: data
  })
}

// 查询床位护理员
export function getBedNurses(bedId) {
  return request({
    url: '/business/nurse/bed/' + bedId,
    method: 'get'
  })
}
