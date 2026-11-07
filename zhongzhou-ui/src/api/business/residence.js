import request from '@/utils/request'

// 楼层/房间/床位/老人聚合视图
export function getOverview(floorNo) {
  return request({
    url: '/business/residence/overview',
    method: 'get',
    params: { floorNo }
  })
}

// 房间详情（含床位老人列表与入住率）
export function getRoomDetail(roomId) {
  return request({
    url: '/business/residence/room/' + roomId,
    method: 'get'
  })
}

// 查询空闲床位列表
export function getAvailableBeds() {
  return request({
    url: '/business/residence/available-beds',
    method: 'get'
  })
}
