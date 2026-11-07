import request from '@/utils/request'

// 查询老人列表
export function listElder(query) {
  return request({
    url: '/business/elder/list',
    method: 'get',
    params: query
  })
}

// 查询老人详细
export function getElder(id) {
  return request({
    url: '/business/elder/' + id,
    method: 'get'
  })
}
