import request from '@/utils/request'

// 查询投诉建议列表
export function listFeedback(query) {
  return request({
    url: '/business/feedback/list',
    method: 'get',
    params: query
  })
}

// 查询投诉建议详细
export function getFeedback(id) {
  return request({
    url: '/business/feedback/' + id,
    method: 'get'
  })
}

// 回复投诉建议
export function replyFeedback(data) {
  return request({
    url: '/business/feedback/reply',
    method: 'put',
    data: data
  })
}

// 删除投诉建议
export function delFeedback(ids) {
  return request({
    url: '/business/feedback/' + ids,
    method: 'delete'
  })
}
