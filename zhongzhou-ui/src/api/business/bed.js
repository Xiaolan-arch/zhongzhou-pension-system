import request from '@/utils/request'

// 查询床位列表（聚合查询：含房间号、老人、护理员等）
export function listBed(query) {
  return request({
    url: '/business/bed/list',
    method: 'get',
    params: query
  })
}

// 查询床位详细
export function getBed(bedId) {
  return request({
    url: '/business/bed/' + bedId,
    method: 'get'
  })
}

// 新增床位
export function addBed(data) {
  return request({
    url: '/business/bed',
    method: 'post',
    data: data
  })
}

// 修改床位
export function updateBed(data) {
  return request({
    url: '/business/bed',
    method: 'put',
    data: data
  })
}

// 删除床位
export function delBed(bedIds) {
  return request({
    url: '/business/bed/' + bedIds,
    method: 'delete'
  })
}
