import request from '@/utils/request'

// 查询服务项目列表
export function listItem(query) {
  return request({
    url: '/business/service/item/list',
    method: 'get',
    params: query
  })
}

// 查询服务项目详细
export function getItem(id) {
  return request({
    url: '/business/service/item/' + id,
    method: 'get'
  })
}

// 新增服务项目
export function addItem(data) {
  return request({
    url: '/business/service/item',
    method: 'post',
    data: data
  })
}

// 修改服务项目
export function updateItem(data) {
  return request({
    url: '/business/service/item',
    method: 'put',
    data: data
  })
}

// 删除服务项目
export function delItem(ids) {
  return request({
    url: '/business/service/item/' + ids,
    method: 'delete'
  })
}

// 查询服务订单列表
export function listOrder(query) {
  return request({
    url: '/business/service/order/list',
    method: 'get',
    params: query
  })
}

// 查询服务订单详细
export function getOrder(id) {
  return request({
    url: '/business/service/order/' + id,
    method: 'get'
  })
}

// 新增服务订单
export function addOrder(data) {
  return request({
    url: '/business/service/order',
    method: 'post',
    data: data
  })
}

// 修改服务订单
export function updateOrder(data) {
  return request({
    url: '/business/service/order',
    method: 'put',
    data: data
  })
}

// 删除服务订单
export function delOrder(ids) {
  return request({
    url: '/business/service/order/' + ids,
    method: 'delete'
  })
}

// 执行服务订单
export function executeOrder(id) {
  return request({
    url: '/business/service/order/execute/' + id,
    method: 'put'
  })
}

// 完成服务订单（可带 serviceRemark 服务记录备注）
export function completeOrder(id, serviceRemark) {
  return request({
    url: '/business/service/order/complete/' + id,
    method: 'put',
    data: { serviceRemark }
  })
}

// 护理员"我的待办"
export function listMyOrders() {
  return request({
    url: '/business/service/order/my',
    method: 'get'
  })
}

// 护理任务聚合视图（床位附加护理员）
export function getTaskOverview(floorNo) {
  return request({
    url: '/business/service/task/overview',
    method: 'get',
    params: { floorNo }
  })
}

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
