import request from '@/utils/request'

// 查询房间列表
export function listRoom(query) {
  return request({
    url: '/business/room/list',
    method: 'get',
    params: query
  })
}

// 查询房间详细
export function getRoom(id) {
  return request({
    url: '/business/room/' + id,
    method: 'get'
  })
}

// 新增房间
export function addRoom(data) {
  return request({
    url: '/business/room',
    method: 'post',
    data: data
  })
}

// 修改房间
export function updateRoom(data) {
  return request({
    url: '/business/room',
    method: 'put',
    data: data
  })
}

// 删除房间
export function delRoom(ids) {
  return request({
    url: '/business/room/' + ids,
    method: 'delete'
  })
}
